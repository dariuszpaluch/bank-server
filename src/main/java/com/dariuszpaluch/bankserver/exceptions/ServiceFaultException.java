package com.dariuszpaluch.bankserver.exceptions;
import com.dariuszpaluch.services.bank.*;

import org.springframework.http.HttpStatus;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class ServiceFaultException extends RuntimeException {

  private ServiceFault serviceFault;

  public ServiceFaultException(HttpStatus code, String description) {
    super("ERROR");
    ServiceFault serviceFault = new ServiceFault();
    serviceFault.setCode(code.name());
    serviceFault.setDescription(description);

    this.serviceFault = serviceFault;
  }

  public ServiceFaultException(Throwable e, HttpStatus code, String description) {
    super("ERROR", e);
    ServiceFault serviceFault = new ServiceFault();
    serviceFault.setCode(code.name());
    serviceFault.setDescription(description);

    this.serviceFault = serviceFault;
  }

  public ServiceFaultException(ServiceFault serviceFault) {
    super("ERROR");
    this.serviceFault = serviceFault;
  }

  public ServiceFaultException(Throwable e, ServiceFault serviceFault) {
    super("ERROR", e);
    this.serviceFault = serviceFault;
  }


  public ServiceFaultException(String message, ServiceFault serviceFault) {
    super(message);
    this.serviceFault = serviceFault;
  }

  public ServiceFaultException(String message, Throwable e, ServiceFault serviceFault) {
    super(message, e);
    this.serviceFault = serviceFault;
  }

  public ServiceFault getServiceFault() {
    return serviceFault;
  }

  public void setServiceFault(ServiceFault serviceFault) {
    this.serviceFault = serviceFault;
  }
}
