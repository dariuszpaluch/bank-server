package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.exceptions.*;
import com.dariuszpaluch.bankserver.models.Account;
import com.dariuszpaluch.bankserver.models.ExternalTransferRequest;
import com.dariuszpaluch.bankserver.models.User;
import com.dariuszpaluch.bankserver.rest.ValidationError;
import com.dariuszpaluch.bankserver.utils.BankAccountUtils;
import com.dariuszpaluch.bankserver.utils.ValidationsUtils;
import com.dariuszpaluch.services.bank.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
@Component
public class BankRepository {
  private static final Map<String, Double> accounts = new HashMap<>();
  private BankDAO bankDAO = BankDAO.getInstance();
  private ValidationsUtils bankVerification = new ValidationsUtils();

  public Balance getBalance(String userToken, String accountNo) throws WrongUserTokenException {
    User user = BankSoapValidations.validUserToken(userToken);
    BankSoapValidations.validGetBalance(user, accountNo);
    try {
      int value = this.bankDAO.getAccount(accountNo).getBalance();
      Balance balance = new Balance();
      balance.setBalance(value);
      balance.setDate(new Date().toString());
      return balance;

    } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
      //handle in Validations
    }

    return null;
  }

  public void depositMoney(String userToken, String accountNo, int amount) throws WrongUserTokenException, AccountNumberDoesNotExist, UserIsNotTheOwnerOfThisAccount, IncorrectAmount, DatabaseException {
    User user = BankSoapValidations.validUserToken(userToken);
    BankSoapValidations.validDepositMoneyRequest(user, accountNo, amount);
//
     this.bankDAO.depositMoney(accountNo, amount);
  }

  public boolean withdrawMoney(String userToken, String accountNo, int amount) throws AccountNumberDoesNotExist, NotEnoughMoneyInAccount, IncorrectAmount, UserIsNotTheOwnerOfThisAccount, WrongUserTokenException, DatabaseException {

    User user = bankVerification.verificationUserByToken(userToken);
    BankSoapValidations.validWithdrawMoneyRequest(user, accountNo, amount);

    this.bankDAO.withdrawMoney(accountNo, amount);
    return true;
  }

  public String createAccount(String userToken) throws WrongUserTokenException, DatabaseException {
    User user = BankSoapValidations.validUserToken(userToken);
    String accountNo = this.bankDAO.createBankAccount(user);
    accounts.put(accountNo, 0.0);


    return accountNo;
  }

  public boolean registerUser(String login, String password) throws DatabaseException, UserLoginIsBusyException {

    BankSoapValidations.validRegisterRequest(login, password);
    this.bankDAO.addUser(login, password);

    return true;
  }

  public String authenticate(String login, String password) throws ServiceFaultException {
    try {
      return this.bankDAO.authenticate(login, password);
    } catch (WrongAuthenticateData wrongAuthenticateData) {
      throw new ServiceFaultException(HttpStatus.UNAUTHORIZED, "Wrong authenticate data.");
    } catch (DatabaseException e) {
      throw new ServiceFaultException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error with authenticate");
    }
  }

  public List<String> getUserAccounts(String userToken) throws WrongUserTokenException, DatabaseException {
    User user = BankSoapValidations.validUserToken(userToken);

    return this.bankDAO.getUserAccounts(user);
  }

  public void makeExternalTransfer(Transfer transfer) {
    try {

      ResponseEntity<ValidationError> response = null;
      response = this.sendJSONTransferToAnotherBank(transfer);

    HttpStatus responseStatusCode = response.getStatusCode();

    switch(responseStatusCode) {
      case CREATED: {
          this.bankDAO.executeTransferToAnotherBank(transfer);
        break;
      }
      case BAD_REQUEST: {
        ValidationError validationError = response.getBody();
        throw new ServiceFaultException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error with another Bank. Bank response[ code:" + responseStatusCode.toString() + " error:" + validationError.getError() + " " + "error_field:" + validationError.getError_field());
      }
      default: {
        throw new ServiceFaultException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error with another Bank. Bank response[ code:" + responseStatusCode.toString());
      }
    }
    } catch (DatabaseException e) {
      e.printStackTrace();
      throw new ServiceFaultException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error with withdraw money");
    } catch (AccountNumberDoesNotExist e) {
      e.printStackTrace();
      throw new ServiceFaultException(HttpStatus.NOT_FOUND, "This accounts doesn't exist");
    } catch (WrongBankIdInExternalTransfer wrongBankIdInExternalTransfer) {
      throw new ServiceFaultException(HttpStatus.NOT_FOUND, "Wrong BankId in destination accountNo, I don't have address of this Bank");
    }
  }

  public void makeTransfer(String userToken, Transfer transfer) throws ServiceFaultException, WrongUserTokenException {
    User user = BankSoapValidations.validUserToken(userToken);
    BankSoapValidations.validInternalTransfer(user, transfer);

    String destinationAccount = transfer.getDestinationAccount();
    String destinationBankId = BankAccountUtils.getBankIdFromAccountNo(destinationAccount);


    if (destinationBankId.equals(Settings.MY_BANK_ID)) {

      try {
        this.bankDAO.makeInternalTransfer(transfer);
      } catch (DatabaseException e) {
        throw new ServiceFaultException(HttpStatus.NOT_FOUND, "Some error with database");
      } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
        //validEarlier
      }

    } else {
      this.makeExternalTransfer(transfer);
    }
  }

  private ResponseEntity<ValidationError> sendJSONTransferToAnotherBank(Transfer transfer) throws WrongBankIdInExternalTransfer {
    RestTemplateBuilder builder = new RestTemplateBuilder();
    RestTemplate restTemplate = builder.basicAuthorization(Settings.EXTERNAL_BANK_AUTHORIZATION_LOGIN, Settings.EXTERNAL_BANK_AUTHORIZATION_PASSWORD).build();
    ExternalTransferRequest externalTransferRequest = new ExternalTransferRequest();
    externalTransferRequest.setAmount(transfer.getAmount());
    externalTransferRequest.setName(transfer.getName());
    externalTransferRequest.setSource_account(transfer.getSourceAccount());
    externalTransferRequest.setTitle(transfer.getTitle());

    String destinationBankId = BankAccountUtils.getBankIdFromAccountNo(transfer.getDestinationAccount());
    String bankUrl = Settings.getBankUrl(destinationBankId);

    System.out.println(bankUrl + "/accounts/" + transfer.getDestinationAccount() + "/history");
    return restTemplate.postForEntity(bankUrl + "/accounts/" + transfer.getDestinationAccount() + "/history", externalTransferRequest, ValidationError.class);
  }

  public List<OperationHistory> getAccountHistory(String userToken, String accountNo) throws WrongUserTokenException, DatabaseException {
    User user = BankSoapValidations.validUserToken(userToken);
    BankSoapValidations.validGetAccountHistoryRequest(user, accountNo);

    return this.bankDAO.getAccountHistory(accountNo);
  }
}
