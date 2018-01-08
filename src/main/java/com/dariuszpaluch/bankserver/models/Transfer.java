package com.dariuszpaluch.bankserver.models;

import org.hibernate.validator.constraints.NotBlank;

/**
 * Created by Dariusz Paluch on 08.01.2018.
 */
public class Transfer {
  private Integer amount;

  private String source_account;
  private String destination_account;
  private String title;

  private String name;

  public Transfer() {
  }

  public Transfer(Integer amount, String source_account, String destination_account, String title, String name) {
    this.amount = amount;
    this.source_account = source_account;
    this.destination_account = destination_account;
    this.title = title;
    this.name = name;
  }



  public Integer getAmount() {
    return amount;
  }

  public void setAmount(Integer amount) {
    this.amount = amount;
  }

  public String getSource_account() {
    return source_account;
  }

  public void setSource_account(String source_account) {
    this.source_account = source_account;
  }

  public String getDestination_account() {
    return destination_account;
  }

  public void setDestination_account(String destination_account) {
    this.destination_account = destination_account;
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
