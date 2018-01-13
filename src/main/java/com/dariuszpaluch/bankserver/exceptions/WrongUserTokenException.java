package com.dariuszpaluch.bankserver.exceptions;

/**
 * Created by Dariusz Paluch on 13.01.2018.
 */
public class WrongUserTokenException extends Exception {
  public WrongUserTokenException() {
    super("Wrong user token.");
  }
}
