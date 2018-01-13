package com.dariuszpaluch.bankserver.exceptions;

/**
 * Created by Dariusz Paluch on 13.01.2018.
 */
public class UserIsNotTheOwnerOfThisAccount extends Exception {

  public UserIsNotTheOwnerOfThisAccount() {
    super("User is not the owner of this account");
  }
}
