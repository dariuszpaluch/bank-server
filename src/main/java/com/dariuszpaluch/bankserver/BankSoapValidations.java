package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.exceptions.*;
import com.dariuszpaluch.bankserver.models.User;
import com.dariuszpaluch.bankserver.utils.ValidationsUtils;
import com.dariuszpaluch.services.bank.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

import javax.xml.ws.Service;

/**
 * Created by Dariusz Paluch on 13.01.2018.
 */
public class BankSoapValidations {

  public static User validUserToken(String userToken) throws WrongUserTokenException {
    return BankDAO.getInstance().getUserByToken(userToken);
  }

  public static void validRegisterRequest(String login, String password) {
    Assert.notNull(login, "Login is required");
    Assert.notNull(password, "Password is required");
    Assert.hasText(login, "Login is required");
    Assert.hasText(password, "Password is required");
    Assert.isTrue(password.length() >= 5, "Password must be at least 5 characters long");
    Assert.isTrue(login.length() >= 5, "Login must be at least 5 characters long");
  }

  public static void checkIfUserIsOwnerOfAccount(User user, String accountNo) {
    try {
      ValidationsUtils.verificationIfUserIsOwnerAccountNo(user.getId(), accountNo);
    } catch (UserIsNotTheOwnerOfThisAccount | AccountNumberDoesNotExist userIsNotTheOwnerOfThisAccount) {
      throw new ServiceFaultException(HttpStatus.NOT_FOUND, "User don't have account with this number.");
    }
  }

  public static void validGetBalance(User user, String accountNo) {
    Assert.hasText(accountNo, "Account is required");
    checkIfUserIsOwnerOfAccount(user, accountNo);
  }


  public static void validInternalTransfer(User user, Transfer transfer) throws ServiceFaultException {
    try {
      ValidationsUtils.verificationAmount(transfer.getAmount());
      ValidationsUtils.verificationIfUserIsOwnerAccountNo(user.getId(), transfer.getSourceAccount());
      ValidationsUtils.verificationUserHaveEnoughMoneyInAccount(transfer.getSourceAccount(), transfer.getAmount());
    } catch (IncorrectAmount incorrectAmount) {
      throw new ServiceFaultException(HttpStatus.BAD_REQUEST, "Incorrect amount.");
    } catch (UserIsNotTheOwnerOfThisAccount | AccountNumberDoesNotExist userIsNotTheOwnerOfThisAccount) {
      throw new ServiceFaultException(HttpStatus.NOT_FOUND, "User don't have account with this number.");
    } catch (NotEnoughMoneyInAccount notEnoughMoneyInAccount) {
      throw new ServiceFaultException(HttpStatus.BAD_REQUEST, "You don't have enough money on this account.");
    }
  }

  public static void validWithdrawMoneyRequest(User user, String accountNo, int amount) throws IncorrectAmount, UserIsNotTheOwnerOfThisAccount, AccountNumberDoesNotExist, NotEnoughMoneyInAccount {
    ValidationsUtils.verificationAmount(amount);
    ValidationsUtils.verificationIfUserIsOwnerAccountNo(user.getId(), accountNo);
    ValidationsUtils.verificationUserHaveEnoughMoneyInAccount(accountNo, amount);
  }

  public static void validDepositMoneyRequest(User user, String accountNo, int amount) throws IncorrectAmount, UserIsNotTheOwnerOfThisAccount, AccountNumberDoesNotExist {
    ValidationsUtils.verificationAmount(amount);
    ValidationsUtils.verificationIfUserIsOwnerAccountNo(user.getId(), accountNo);
  }

  public static void validGetAccountHistoryRequest(User user, String accountNo) {
    Assert.hasText(accountNo, "Account is required");
    checkIfUserIsOwnerOfAccount(user, accountNo);
  }
}
