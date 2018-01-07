package com.dariuszpaluch.bankserver.rest;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * Created by Dariusz Paluch on 07.01.2018.
 */
public class ValidationErrorBuilder {
  public static ValidationError fromBindingErrors(Errors errors) {
    FieldError error = (FieldError) errors.getAllErrors().get(0);
    return new ValidationError(error.getField(), error.getDefaultMessage());
  }

}
