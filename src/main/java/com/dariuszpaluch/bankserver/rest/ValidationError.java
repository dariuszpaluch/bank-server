package com.dariuszpaluch.bankserver.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dariusz Paluch on 07.01.2018.
 */
public class ValidationError {
  private String error_field;
  private String error;

  public ValidationError(String error_field, String error) {
    this.error_field = error_field;
    this.error = error;
  }

  public String getError_field() {

    return error_field;
  }

  public void setError_field(String error_field) {
    this.error_field = error_field;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
