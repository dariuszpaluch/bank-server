package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.exceptions.*;
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
import org.springframework.ws.soap.SoapFaultException;

import java.util.*;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
@Component
public class BankRepository {
  private static final Map<String, Double> accounts = new HashMap<>();
  private BankDAO bankDAO = BankDAO.getInstance();
  private ValidationsUtils bankVerification = new ValidationsUtils();

  public Balance getBalance(String userToken, String accountNo) throws SoapFaultException {
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

  public void depositMoney(String userToken, String accountNo, int amount) throws SoapFaultException {
    User user = BankSoapValidations.validUserToken(userToken);
    BankSoapValidations.validDepositMoneyRequest(user, accountNo, amount);
//
    try {
      this.bankDAO.depositMoney(accountNo, amount);
    } catch (DatabaseException e) {
      throw new SoapFaultException("Some problem with database");

    } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
      //handle in Validations
    }
  }

  public boolean withdrawMoney(String userToken, String accountNo, int amount) throws SoapFaultException {
    User user = BankSoapValidations.validUserToken(userToken);
    BankSoapValidations.validWithdrawMoneyRequest(user, accountNo, amount);

    try {
      this.bankDAO.withdrawMoney(accountNo, amount);
    } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
      //handle in Validations
    } catch (DatabaseException e) {
      throw new SoapFaultException("Some problem with database");
    }
    return true;
  }

  public String createAccount(String userToken) throws SoapFaultException {
    User user = BankSoapValidations.validUserToken(userToken);
    String accountNo = null;
    try {
      accountNo = this.bankDAO.createBankAccount(user);
    } catch (DatabaseException e) {
      throw new SoapFaultException("Some problem with database");
    }
    accounts.put(accountNo, 0.0);

    return accountNo;
  }

  public boolean registerUser(String login, String password) throws SoapFaultException  {

    BankSoapValidations.validRegisterRequest(login, password);
    try {
      this.bankDAO.addUser(login, password);
    } catch (UserLoginIsBusyException e) {
      throw new SoapFaultException("This login is busy. Change login.");
    } catch (DatabaseException e) {
      throw new SoapFaultException("Some problem with database");
    }

    return true;
  }

  public String authenticate(String login, String password) throws SoapFaultException {
    try {
      BankSoapValidations.validAuthenticateRequest(login, password);
      return this.bankDAO.authenticate(login, password);
    } catch (WrongAuthenticateData wrongAuthenticateData) {
      throw new SoapFaultException("Wrong authenticate data.");
    } catch (DatabaseException e) {
      throw new SoapFaultException("Some problem with database");
    }
  }

  public List<String> getUserAccounts(String userToken)  throws SoapFaultException {
    User user = BankSoapValidations.validUserToken(userToken);

    try {
      return this.bankDAO.getUserAccounts(user);
    } catch (DatabaseException e) {
      throw new SoapFaultException("Some problem with database");
    }
  }

  public void makeTransfer(String userToken, Transfer transfer) throws SoapFaultException {
    User user = BankSoapValidations.validUserToken(userToken);
    Assert.hasText(transfer.getDestinationAccount(), "Destination account is required.");
    Assert.hasText(transfer.getSourceAccount(), "Source account is required.");

    if(transfer.getDestinationAccount().equals(transfer.getSourceAccount())) {
      throw new SoapFaultException("You can't transfer money to the same account.");
    }
    String destinationAccount = transfer.getDestinationAccount();
    String destinationBankId = BankAccountUtils.getBankIdFromAccountNo(destinationAccount);


    if (destinationBankId.equals(Settings.MY_BANK_ID)) {
      try {
        BankSoapValidations.validInternalTransfer(user, transfer);
        this.bankDAO.makeInternalTransfer(transfer);
      } catch (DatabaseException e) {
        throw new SoapFaultException("Some error with database");
      } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
        //validEarlier
      }
    } else {
      this.makeExternalTransfer(transfer);
    }
  }

  public void makeExternalTransfer(Transfer transfer) {
    try {
      ResponseEntity<ValidationError> response = null;
      response = this.sendJSONTransferToAnotherBank(transfer);

    HttpStatus responseStatusCode = response.getStatusCode();

    switch(responseStatusCode) {
      case CREATED: {
          this.bankDAO.afterSuccessExecuteTransferToAnotherBank(transfer);
        break;
      }
      case BAD_REQUEST: {
        ValidationError validationError = response.getBody();
        throw new SoapFaultException("Some error with another Bank. Bank response[ code:" + responseStatusCode.toString() + " error:" + validationError.getError() + " " + "error_field:" + validationError.getError_field());
      }
      default: {
        throw new SoapFaultException("Some error with another Bank. Bank response[ code:" + responseStatusCode.toString());
      }
    }
    } catch (DatabaseException e) {
      throw new SoapFaultException("Some error with database");
    } catch (AccountNumberDoesNotExist e) {
      // valid earlier
      throw new SoapFaultException("This accounts doesn't exist");
    } catch (WrongBankIdInExternalTransfer wrongBankIdInExternalTransfer) {
      throw new SoapFaultException( "Wrong BankId in destination accountNo, Bank doesn't have address of this Bank");
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
