package com.dariuszpaluch.bankserver.models;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class Account {
  private String accountNO;
  private int userId;
  private double balance;

  public Account() {
  }

  public Account(String accountNO, int userId, double balance) {

    this.accountNO = accountNO;
    this.userId = userId;
    this.balance = balance;
  }

  public String getAccountNO() {

    return accountNO;
  }

  public void setAccountNO(String accountNO) {
    this.accountNO = accountNO;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public double getBalance() {
    return balance;
  }

  public void setBalance(double balance) {
    this.balance = balance;
  }
}
