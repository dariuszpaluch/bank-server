package com.dariuszpaluch.bankserver.models;

/**
 * Created by Dariusz Paluch on 09.01.2018.
 */
public class BankAddress {
  private String id;
  private String url;

  public BankAddress(String id, String url) {
    this.id = id;
    this.url = url;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
