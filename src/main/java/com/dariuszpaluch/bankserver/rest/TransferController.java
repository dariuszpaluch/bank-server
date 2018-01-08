package com.dariuszpaluch.bankserver.rest;

import com.dariuszpaluch.bankserver.Settings;
import com.dariuszpaluch.bankserver.utils.BankAccountUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
@RestController
@RequestMapping("/accounts")
public class TransferController {
  private static final String template = "Hello, %s!";

  @RequestMapping(value="/{destinationAccount}/history", method = RequestMethod.POST)
  public ResponseEntity transfer(@Valid @RequestBody Transfer transfer, @PathVariable("destinationAccount") String destinationAccount) {

    if(!BankAccountUtils.validateIBAN(destinationAccount)) {
      return ResponseEntity.badRequest().body(new ValidationError("url destinationAccount", "Is not validate IBAN"));
    }

    if(!BankAccountUtils.validateIBAN(transfer.getSource_account())) {
      return ResponseEntity.badRequest().body(new ValidationError("source_account", "Is not validate IBAN"));
    }

    if(!BankAccountUtils.checkIfAccontHaveMyBankId(destinationAccount)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //TODO implement save in database
    return ResponseEntity.status(HttpStatus.OK).build();

  }

  @ExceptionHandler
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public ValidationError handleException(MethodArgumentNotValidException exception) {
    return createValidationError(exception);
  }

  private ValidationError createValidationError(MethodArgumentNotValidException e) {
    return ValidationErrorBuilder.fromBindingErrors(e.getBindingResult());
  }
}
