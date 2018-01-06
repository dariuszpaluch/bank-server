package com.dariuszpaluch.bankserver.exceptions;

import io.spring.guides.gs_producing_web_service.ServiceFault;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import javax.xml.namespace.QName;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class DetailSoapFaultDefinitionExceptionResolver extends SoapFaultMappingExceptionResolver {

  private static final QName CODE = new QName("code");
  private static final QName DESCRIPTION = new QName("description");

  @Override
  protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
    logger.warn("Exception processed ", ex);
    if (ex instanceof ServiceFaultException) {
      ServiceFault serviceFault = ((ServiceFaultException) ex).getServiceFault();
      SoapFaultDetail detail = fault.addFaultDetail();
      detail.addFaultDetailElement(CODE).addText(serviceFault.getCode());
      detail.addFaultDetailElement(DESCRIPTION).addText(serviceFault.getDescription());
    }
  }

}
