package com.dariuszpaluch.bankserver.rest;

import com.dariuszpaluch.bankserver.BankDAO;
import com.dariuszpaluch.bankserver.exceptions.AccountNumberDoesNotExist;
import com.dariuszpaluch.bankserver.exceptions.DatabaseException;
import com.dariuszpaluch.bankserver.exceptions.IncorrectAmount;
import com.dariuszpaluch.bankserver.models.ExternalTransferRequest;
import com.dariuszpaluch.bankserver.utils.BankAccountUtils;
import com.dariuszpaluch.bankserver.utils.ValidationsUtils;
import com.dariuszpaluch.services.bank.Transfer;
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
  public ResponseEntity transfer(@Valid @RequestBody ExternalTransferRequest request, @PathVariable("destinationAccount") String destinationAccount) {

    try {
      ValidationsUtils.verificationAmount(request.getAmount());
    } catch (IncorrectAmount incorrectAmount) {
      return ResponseEntity.badRequest().body(new ValidationError("amount", "Wrong amount value."));
    }
    System.out.println("GET EXTERNAL TRANSFER");
    if (!BankAccountUtils.validateIBAN(destinationAccount)) {
      return ResponseEntity.badRequest().body(new ValidationError("url destinationAccount", "Is not validate IBAN"));
    }

    if (!BankAccountUtils.validateIBAN(request.getSource_account())) {
      return ResponseEntity.badRequest().body(new ValidationError("source_account", "Is not validate IBAN"));
    }

    if (!BankAccountUtils.checkIfAccontHaveMyBankId(destinationAccount)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    //TODO implement save in database
    Transfer transfer = new Transfer();
    transfer.setAmount(request.getAmount());
    transfer.setSourceAccount(request.getSource_account());
    transfer.setDestinationAccount(destinationAccount);
    transfer.setTitle(request.getTitle());
    transfer.setName(request.getName());

    try {
      this.bankDAO.externalIncomingTransfer(transfer);
    } catch (DatabaseException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    } catch (AccountNumberDoesNotExist accountNumberDoesNotExist) {
      return ResponseEntity.badRequest().body(new ValidationError("url destinationAccount", "This accountNo doesn't exist in my bank."));
    }
    return ResponseEntity.status(HttpStatus.CREATED).build();

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
