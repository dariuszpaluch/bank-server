package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.exceptions.ServiceFaultException;
import com.dariuszpaluch.bankserver.models.Account;
import com.dariuszpaluch.bankserver.models.User;
import org.springframework.http.HttpStatus;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class BankVerificationData {
  private BankDAO bankDAO = BankDAO.getInstance();

  public User verificationUserByToken(String userToken) throws ServiceFaultException {
    try {
      return this.bankDAO.getUserByToken(userToken);
    } catch (Exception e) {
      throw new ServiceFaultException(HttpStatus.UNAUTHORIZED, "Wrong token");
    }
  }

  public void verificationAmount(double amount) throws ServiceFaultException {
    if(amount <= 0) {
      throw new ServiceFaultException(HttpStatus.BAD_REQUEST, "Wrong amount");

    }
  }

  public Account verificationIfUserIsOwnerAccountNo(int id, String accountNo) {
    Account account = this.bankDAO.getAccount(accountNo);

    if(account.getUserId() != id) {
      throw new ServiceFaultException(HttpStatus.BAD_REQUEST, "You don't have account " + accountNo);
    }
    return  account;
  }

  public boolean verificationUserHaveEnoughMoneyInAccount(Account account, double amount) {
    if(amount > account.getBalance()) {
      throw new ServiceFaultException(HttpStatus.BAD_REQUEST, "You don't enough moeny. Your balance: " + account.getBalance());
    }

    return true;
  }
}
