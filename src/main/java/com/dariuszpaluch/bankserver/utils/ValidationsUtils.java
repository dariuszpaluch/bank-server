package com.dariuszpaluch.bankserver.utils;

import com.dariuszpaluch.bankserver.BankDAO;
import com.dariuszpaluch.bankserver.exceptions.*;
import com.dariuszpaluch.bankserver.models.Account;
import com.dariuszpaluch.bankserver.models.User;
import org.springframework.http.HttpStatus;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class ValidationsUtils {
  private static BankDAO bankDAO = BankDAO.getInstance();

  public static void verificationAmount(int amount) throws IncorrectAmount {
    if(amount <= 0) {
      throw new IncorrectAmount();
    }
  }

  public static void verificationIfUserIsOwnerAccountNo(int id, String accountNo) throws AccountNumberDoesNotExist, UserIsNotTheOwnerOfThisAccount {
    Account account = bankDAO.getAccount(accountNo);
    if(account.getUserId() != id) {
      throw new UserIsNotTheOwnerOfThisAccount();
    }
  }

  public static void verificationAccountIsExist(String accountNo) throws AccountNumberDoesNotExist {
    Account account = bankDAO.getAccount(accountNo);
  }

  public static  boolean verificationUserHaveEnoughMoneyInAccount(String accountNo, int amount) throws NotEnoughMoneyInAccount, AccountNumberDoesNotExist {
    Account account = bankDAO.getAccount(accountNo);

    if(amount > account.getBalance()) {
      throw new NotEnoughMoneyInAccount();
    }

    return true;
  }
}
