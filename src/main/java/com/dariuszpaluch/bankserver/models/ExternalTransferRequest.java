package com.dariuszpaluch.bankserver.models;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class ExternalTransferRequest {

  private int amount;

  @NotBlank(message = "ExternalTransferRequest source account is required!")
  private String source_account;

  @NotBlank(message = "ExternalTransferRequest title is required!")
  private String title;

  @NotBlank(message = "ExternalTransferRequest name is required!")
  private String name;

  public ExternalTransferRequest() {
  }

  public ExternalTransferRequest(Integer amount, String source_account, String title, String name) {
    this.amount = amount;
    this.source_account = source_account;
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

