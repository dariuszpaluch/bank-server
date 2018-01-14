package com.dariuszpaluch.bankserver.models;

/**
 * Created by Dariusz Paluch on 08.01.2018.
 */

public class BankOperation {
  private int id;
  private String sourceAccount;
  private String destinationAccount;
  private int amount;
  private String title;
  private String name;
  private BankOperationType type;

  public BankOperation() {
  }

  public BankOperation(String sourceAccount, String destinationAccount, int amount, String title, String name,  BankOperationType type) {
    this.sourceAccount = sourceAccount;
    this.destinationAccount = destinationAccount;
    this.amount = amount;
    this.title = title;
    this.type = type;
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public BankOperationType getType() {
    return type;
  }

  public void setType(BankOperationType type) {
    this.type = type;
  }

  public String getSourceAccount() {
    return sourceAccount;
  }

  public void setSourceAccount(String sourceAccount) {
    this.sourceAccount = sourceAccount;
  }

  public String getDestinationAccount() {
    return destinationAccount;
  }

  public void setDestinationAccount(String destinationAccount) {
    this.destinationAccount = destinationAccount;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
