package com.dariuszpaluch.bankserver.SpringConfiguration;

import com.dariuszpaluch.bankserver.exceptions.DetailSoapFaultDefinitionExceptionResolver;
import com.dariuszpaluch.bankserver.exceptions.ServiceFaultException;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import java.util.Properties;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

//  @Bean
//  public SoapFaultMappingExceptionResolver exceptionResolver(){
//    SoapFaultMappingExceptionResolver exceptionResolver = new DetailSoapFaultDefinitionExceptionResolver();
//
//    SoapFaultDefinition faultDefinition = new SoapFaultDefinition();
////    faultDefinition.setFaultCode(SoapFaultDefinition.SERVER);
//    exceptionResolver.setDefaultFault(faultDefinition);
//
//    Properties errorMappings = new Properties();
////    errorMappings.setProperty(Exception.class.getName(), SoapFaultDefinition.SERVER.toString());
//    errorMappings.setProperty(ServiceFaultException.class.getName(), SoapFaultDefinition.SERVER.toString());
//    exceptionResolver.setExceptionMappings(errorMappings);
//    exceptionResolver.setOrder(1);
//    return exceptionResolver;
//  }

  @Bean
  public ServletRegistrationBean messageDispatcherServlet(ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean(servlet, "/ws/*");
  }

  @Bean(name = "bank")
  public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema countriesSchema) {
    DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
    wsdl11Definition.setPortTypeName("BankPort");
    wsdl11Definition.setLocationUri("/ws");
    wsdl11Definition.setTargetNamespace("http://dariuszpaluch.com/services/bank");
    wsdl11Definition.setSchema(countriesSchema);
    return wsdl11Definition;
  }

  @Bean
  public XsdSchema countriesSchema() {
    return new SimpleXsdSchema(new ClassPathResource("bank.xsd"));
  }

  @Bean
  public EmbeddedServletContainerCustomizer containerCustomizer() {
    return (container -> {
      container.setPort(8090);
    });
  }
}
