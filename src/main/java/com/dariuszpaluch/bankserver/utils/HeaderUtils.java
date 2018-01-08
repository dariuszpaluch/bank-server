package com.dariuszpaluch.bankserver.utils;

import com.dariuszpaluch.bankserver.exceptions.ServiceFaultException;
import com.dariuszpaluch.services.bank.*;
import org.springframework.http.HttpStatus;
import org.springframework.ws.soap.SoapHeaderElement;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPHeaderElement;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
public class HeaderUtils {
  public static String getTokenFromHeader(SoapHeaderElement soapHeaderElement) throws ServiceFaultException {
    JAXBContext context = null;
    try {
      context = JAXBContext.newInstance(ObjectFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      JAXBElement<MyHeaders> headers = (JAXBElement<MyHeaders>) unmarshaller.unmarshal(soapHeaderElement.getSource());
      MyHeaders requestSoapHeaders = headers.getValue();
      String token = requestSoapHeaders.getToken();
      return token;
    } catch (JAXBException e) {
      throw new ServiceFaultException(e, HttpStatus.INTERNAL_SERVER_ERROR, "Some error with get token from headers");
    } catch(NullPointerException e) {
      throw new ServiceFaultException(e, HttpStatus.UNAUTHORIZED, "Wrong token");
    }
  }
}
