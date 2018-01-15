package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.exceptions.*;
import com.dariuszpaluch.bankserver.models.User;
import com.dariuszpaluch.bankserver.utils.BankSoapValidationsUtils;
import com.dariuszpaluch.bankserver.utils.ValidationsUtils;
import com.dariuszpaluch.services.bank.Transfer;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.ws.soap.SoapFaultException;

import javax.xml.ws.Service;

/**
 * Created by Dariusz Paluch on 13.01.2018.
 */
public class BankSoapValidations {

  public static User validUserToken(String userToken) throws SoapFaultException  {
    try {
      return BankDAO.getInstance().getUserByToken(userToken);
    } catch (WrongUserTokenException e) {
      throw new SoapFaultException("Incorrect user token.");
    }
  }

  public static void validAuthenticateRequest(String login, String password) {
    Assert.notNull(login, "Login is required");
    Assert.notNull(password, "Password is required");
    Assert.hasText(login, "Login is required");
    Assert.hasText(password, "Password is required");
  }

  public static void validRegisterRequest(String login, String password) {
    Assert.notNull(login, "Login is required");
    Assert.notNull(password, "Password is required");
    Assert.hasText(login, "Login is required");
    Assert.hasText(password, "Password is required");
    Assert.isTrue(password.length() >= 5, "Password must be at least 5 characters long");
    Assert.isTrue(login.length() >= 5, "Login must be at least 5 characters long");
  }

  public static void validGetBalance(User user, String accountNo) {
    BankSoapValidationsUtils.validOwnerAccount(user, accountNo);
  }

  public static void validInternalTransfer(User user, Transfer transfer) throws SoapFaultException {
    BankSoapValidationsUtils.validAmountOnAccount(user, transfer.getSourceAccount(), transfer.getAmount());
    BankSoapValidationsUtils.validAccountIsExist(transfer.getDestinationAccount());
  }

  public static void validWithdrawMoneyRequest(User user, String accountNo, int amount) throws SoapFaultException {
    BankSoapValidationsUtils.validAmountOnAccount(user, accountNo, amount);
  }

  public static void validDepositMoneyRequest(User user, String accountNo, int amount) {
    BankSoapValidationsUtils.validAmount(amount);
    BankSoapValidationsUtils.validOwnerAccount(user, accountNo);
  }

  public static void validGetAccountHistoryRequest(User user, String accountNo) {
    BankSoapValidationsUtils.validOwnerAccount(user, accountNo);
  }
}
