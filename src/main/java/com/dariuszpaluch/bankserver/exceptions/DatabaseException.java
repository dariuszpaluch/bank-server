package com.dariuszpaluch.bankserver.exceptions;

/**
 * Created by Dariusz Paluch on 08.01.2018.
 */
public class DatabaseException extends Exception {

  public DatabaseException() {
    super("Some problem with database");
  }
}
