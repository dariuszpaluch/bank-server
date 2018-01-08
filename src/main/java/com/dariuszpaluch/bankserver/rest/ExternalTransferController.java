package com.dariuszpaluch.bankserver.rest;

import com.dariuszpaluch.bankserver.BankDAO;
import com.dariuszpaluch.bankserver.exceptions.AccountNumberDoesNotExist;
import com.dariuszpaluch.bankserver.exceptions.DatabaseException;
import com.dariuszpaluch.bankserver.models.ExternalTransferRequest;
import com.dariuszpaluch.bankserver.models.Transfer;
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
public class ExternalTransferController {
  private BankDAO bankDAO = BankDAO.getInstance();

  @RequestMapping(value = "/{destinationAccount}/history", method = RequestMethod.POST)
  public ResponseEntity transfer(@Valid @RequestBody ExternalTransferRequest externalIncomingTransferRequest, @PathVariable("destinationAccount") String destinationAccount) {

    if (!BankAccountUtils.validateIBAN(destinationAccount)) {
      return ResponseEntity.badRequest().body(new ValidationError("url destinationAccount", "Is not validate IBAN"));
    }

    if (!BankAccountUtils.validateIBAN(externalIncomingTransferRequest.getSource_account())) {
      return ResponseEntity.badRequest().body(new ValidationError("source_account", "Is not validate IBAN"));
    }

    if (!BankAccountUtils.checkIfAccontHaveMyBankId(destinationAccount)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //TODO implement save in database
    Transfer transfer = new Transfer(
            externalIncomingTransferRequest.getAmount(),
            externalIncomingTransferRequest.getSource_account(),
            destinationAccount,
            externalIncomingTransferRequest.getTitle(),
            externalIncomingTransferRequest.getName()
    );

    try {
      this.bankDAO.externalIncomingTransfer(transfer);
    } catch (DatabaseException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
      return ResponseEntity.badRequest().body(new ValidationError("url destinationAccount", "This accountNo doesn't exist in my bank."));
    }
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