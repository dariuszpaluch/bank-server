package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.exceptions.AccountNumberDoesNotExist;
import com.dariuszpaluch.bankserver.exceptions.DatabaseException;
import com.dariuszpaluch.bankserver.exceptions.ServiceFaultException;
import com.dariuszpaluch.bankserver.exceptions.WrongBankIdInExternalTransfer;
import com.dariuszpaluch.bankserver.models.Account;
import com.dariuszpaluch.bankserver.models.ExternalTransferRequest;
import com.dariuszpaluch.bankserver.models.User;
import com.dariuszpaluch.bankserver.rest.ValidationError;
import com.dariuszpaluch.bankserver.utils.BankAccountUtils;
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
  private BankVerificationData bankVerification = new BankVerificationData();

  public Balance getBalance(String userToken, String accountNo) {
    User user = bankVerification.verificationUserByToken(userToken);
    Account account = bankVerification.verificationIfUserIsOwnerAccountNo(user.getId(), accountNo);

    int balanceValue = account.getBalance();
    Balance balance = new Balance();
    balance.setDate(new Date().toString());
    balance.setBalance(balanceValue);
    return balance;
  }

  public void depositMoney(String userToken, String accountNo, int amount) throws ServiceFaultException {
    User user = bankVerification.verificationUserByToken(userToken);
    bankVerification.verificationAmount(amount);
    Account account = bankVerification.verificationIfUserIsOwnerAccountNo(user.getId(), accountNo);

    try {
     this.bankDAO.depositMoney(accountNo, amount);
    } catch (DatabaseException e) {
      throw new ServiceFaultException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error with deposit money");
    } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
      throw new ServiceFaultException(HttpStatus.NOT_FOUND, "This accounts doesn't exist");
    }
  }

  public boolean withdrawMoney(String userToken, String accountNo, int amount) {
    User user = bankVerification.verificationUserByToken(userToken);
    bankVerification.verificationAmount(amount);
    Account account = bankVerification.verificationIfUserIsOwnerAccountNo(user.getId(), accountNo);
    bankVerification.verificationUserHaveEnoughMoneyInAccount(account, amount);

    try {
      this.bankDAO.withdrawMoney(accountNo, amount);
    } catch (DatabaseException e) {
      e.printStackTrace();
      throw new ServiceFaultException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error with withdraw money");
    } catch (AccountNumberDoesNotExist e) {
      e.printStackTrace();
      throw new ServiceFaultException(HttpStatus.NOT_FOUND, "This accounts doesn't exist");
    }

    return true;
  }

  public String createAccount(String token) throws Exception {
//    String accountNo = String.valueOf(accounts.size() + 1);

    String accountNo = this.bankDAO.createBankAccount(token);
    accounts.put(accountNo, 0.0);

    return accountNo;
  }

  public boolean registerUser(String login, String password) {
    Assert.notNull(login, "Login is required");
    Assert.notNull(password, "Password is required");

    this.bankDAO.addUser(login, password);

    return true;
  }

  public String authenticate(String login, String password) throws Exception {
    Assert.notNull(login, "Login is required");
    Assert.notNull(password, "Password is required");

    try {
      String token = this.bankDAO.authenticate(login, password);
      return token;
    } catch (Exception e) {
      throw new ServiceFaultException(HttpStatus.UNAUTHORIZED, "Wrong login or password");
    }
  }

  public List<String> getUserAccounts(String userToken) {
    User user = bankVerification.verificationUserByToken(userToken);

    try {
      return this.bankDAO.getUserAccounts(user);

    } catch (DatabaseException e) {
      e.printStackTrace();
      throw new ServiceFaultException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error with withdraw money");
    }
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

  public void makeTransfer(String userToken, Transfer transfer) {

    User user = bankVerification.verificationUserByToken(userToken);
    bankVerification.verificationAmount(transfer.getAmount());
    Account account = bankVerification.verificationIfUserIsOwnerAccountNo(user.getId(), transfer.getSourceAccount());
    String destinationAccount = transfer.getDestinationAccount();
    bankVerification.verificationUserHaveEnoughMoneyInAccount(account, transfer.getAmount());

    String destinationBankId = BankAccountUtils.getBankIdFromAccountNo(destinationAccount);

    System.out.println(destinationBankId);

//    if(destinationBankId.equals(Settings.MY_BANK_ID)) {
//      try {
//        this.bankDAO.makeInternalTransfer(transfer);
//      } catch (DatabaseException e) {
//        throw new ServiceFaultException(HttpStatus.NOT_FOUND, "Some error with database");
//      } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
//        throw new ServiceFaultException(HttpStatus.NOT_FOUND, "account number doesn't exist");
//      }
//    }
//    else {
      this.makeExternalTransfer(transfer);
//    }
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
}
