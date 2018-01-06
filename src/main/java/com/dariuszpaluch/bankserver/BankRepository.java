package com.dariuszpaluch.bankserver;

import io.spring.guides.gs_producing_web_service.Balance;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
@Component
public class BankRepository {
  private static final Map<String, Double> accounts = new HashMap<>();
  private BankDAO bankDAO = BankDAO.getInstance();

  @PostConstruct
  public void initData() {
    accounts.put("1", 100.0);
    accounts.put("2", 300.0);
  }


  public Balance getBalance(String accountNo) {
    Assert.notNull(accountNo, "The acoount number must not be null");
    Assert.isTrue(accounts.containsKey(accountNo), "The account with this number doesn't exist");

    double balanceValue = accounts.get(accountNo);
    System.out.println(balanceValue);

    Balance balance = new Balance();
    balance.setDate(new Date().toString());
    balance.setBalance(balanceValue);
    return balance;
  }

  public boolean depositMoney(String accountNo, double amount) {
    Assert.notNull(accountNo, "The acoount number must not be null");
    Assert.isTrue(amount >= 0, "Incorrect amount");
    Assert.isTrue(accounts.containsKey(accountNo), "The account with this number doesn't exist");
    double accountAmount = accounts.get(accountNo);
    accounts.put(accountNo, accountAmount + amount);
    return true;
  }

  public boolean withdrawMoney(String accountNo, double amount) {
    Assert.notNull(accountNo, "The acoount number must not be null");
    Assert.isTrue(amount >= 0, "Incorrect amount");
    Assert.isTrue(accounts.containsKey(accountNo), "The account with this number doesn't exist");
    Assert.isTrue(accounts.get(accountNo) >= amount, "You don't have enough money in account to withdraw this amount");

    double accountAmount = accounts.get(accountNo);
    accounts.put(accountNo, accountAmount - amount);
    return true;
  }

  public String createAccount() {
//    String accountNo = String.valueOf(accounts.size() + 1);

    String accountNo = this.bankDAO.createAccound();
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

    return this.bankDAO.authenticate(login, password);
  }
}
