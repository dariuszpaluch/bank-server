package com.dariuszpaluch.bankserver.utils;

import io.spring.guides.gs_producing_web_service.MyHeaders;
import io.spring.guides.gs_producing_web_service.ObjectFactory;
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
  public static String getTokenFromHeader(SoapHeaderElement soapHeaderElement) {
    JAXBContext context = null;
    try {
      context = JAXBContext.newInstance(ObjectFactory.class);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      JAXBElement<MyHeaders> headers = (JAXBElement<MyHeaders>) unmarshaller.unmarshal(soapHeaderElement.getSource());
      MyHeaders requestSoapHeaders = headers.getValue();
      String token = requestSoapHeaders.getToken();
      return token;
    } catch (JAXBException e) {
      e.printStackTrace();
    }

    return null;
  }

}
