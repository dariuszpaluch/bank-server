package com.dariuszpaluch.bankserver.utils;

import com.dariuszpaluch.bankserver.BankSoapValidations;
import com.dariuszpaluch.bankserver.exceptions.AccountNumberDoesNotExist;
import com.dariuszpaluch.bankserver.exceptions.IncorrectAmount;
import com.dariuszpaluch.bankserver.exceptions.NotEnoughMoneyInAccount;
import com.dariuszpaluch.bankserver.exceptions.UserIsNotTheOwnerOfThisAccount;
import com.dariuszpaluch.bankserver.models.User;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultException;

/**
 * Created by Dariusz Paluch on 15.01.2018.
 */
public class BankSoapValidationsUtils {

  public static void validAmount(int amount) throws SoapFaultException {
    try {
      ValidationsUtils.verificationAmount(amount);
    } catch (IncorrectAmount incorrectAmount) {
      throw new SoapFaultException("Incorrect amount.");
    }
  }

  public static void validOwnerAccount(User user, String accountNo) throws SoapFaultException  {
    Assert.hasText(accountNo, "Account is required.");

    try {
      ValidationsUtils.verificationIfUserIsOwnerAccountNo(user.getId(), accountNo);
    } catch (AccountNumberDoesNotExist | UserIsNotTheOwnerOfThisAccount accountNumberDoesNotExist) {
      throw new SoapFaultException("User don't have account with this number.");
    }
  }

  public static void validAccountIsExist(String account)  throws SoapFaultException {
    Assert.hasText(account, "Account is required.");
    try {
      ValidationsUtils.verificationAccountIsExist(account);
    } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
      throw new SoapFaultException("Destination account don't exist.");
    }
  }

  public static void validAmountOnAccount(User user, String accountNo, int amount) throws SoapFaultException {
    BankSoapValidationsUtils.validAmount(amount);
    BankSoapValidationsUtils.validOwnerAccount(user, accountNo);

    try {
      ValidationsUtils.verificationUserHaveEnoughMoneyInAccount(accountNo, amount);
    } catch (AccountNumberDoesNotExist userIsNotTheOwnerOfThisAccount) {
//      throw new SoapFaultException("User don't have account with this number.");
    } catch (NotEnoughMoneyInAccount notEnoughMoneyInAccount) {
      throw new SoapFaultException("You don't have enough money on this account.");
    }
  }
}
