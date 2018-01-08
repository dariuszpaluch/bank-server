package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.exceptions.AccountNumberDoesNotExist;
import com.dariuszpaluch.bankserver.exceptions.DatabaseException;
import com.dariuszpaluch.bankserver.exceptions.ServiceFaultException;
import com.dariuszpaluch.bankserver.models.Account;
import com.dariuszpaluch.bankserver.models.Transfer;
import com.dariuszpaluch.bankserver.models.User;
import io.spring.guides.gs_producing_web_service.Balance;
import io.spring.guides.gs_producing_web_service.ServiceFault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     this.bankDAO.depositMoney(user.getId(), accountNo, amount);
    } catch (DatabaseException e) {
      throw new ServiceFaultException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error with deposit money");
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
      throw new ServiceFaultException(HttpStatus.INTERNAL_SERVER_ERROR, "Some error with get list of accounts");
    }
  }
}
