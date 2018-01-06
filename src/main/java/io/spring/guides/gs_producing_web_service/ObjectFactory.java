//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.01.06 at 08:47:34 PM CET 
//


package io.spring.guides.gs_producing_web_service;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the io.spring.guides.gs_producing_web_service package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MyHeaders_QNAME = new QName("http://spring.io/guides/gs-producing-web-service", "myHeaders");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: io.spring.guides.gs_producing_web_service
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CreateAccountResponse }
     * 
     */
    public CreateAccountResponse createCreateAccountResponse() {
        return new CreateAccountResponse();
    }

    /**
     * Create an instance of {@link WithdrawMoneyResponse }
     * 
     */
    public WithdrawMoneyResponse createWithdrawMoneyResponse() {
        return new WithdrawMoneyResponse();
    }

    /**
     * Create an instance of {@link DepositMoneyResponse }
     * 
     */
    public DepositMoneyResponse createDepositMoneyResponse() {
        return new DepositMoneyResponse();
    }

    /**
     * Create an instance of {@link AuthenticateResponse }
     * 
     */
    public AuthenticateResponse createAuthenticateResponse() {
        return new AuthenticateResponse();
    }

    /**
     * Create an instance of {@link GetBalanceRequest }
     * 
     */
    public GetBalanceRequest createGetBalanceRequest() {
        return new GetBalanceRequest();
    }

    /**
     * Create an instance of {@link MyHeaders }
     * 
     */
    public MyHeaders createMyHeaders() {
        return new MyHeaders();
    }

    /**
     * Create an instance of {@link WithdrawMoneyRequest }
     * 
     */
    public WithdrawMoneyRequest createWithdrawMoneyRequest() {
        return new WithdrawMoneyRequest();
    }

    /**
     * Create an instance of {@link AuthenticateRequest }
     * 
     */
    public AuthenticateRequest createAuthenticateRequest() {
        return new AuthenticateRequest();
    }

    /**
     * Create an instance of {@link UserAuthenticateData }
     * 
     */
    public UserAuthenticateData createUserAuthenticateData() {
        return new UserAuthenticateData();
    }

    /**
     * Create an instance of {@link RegisterUserRequest }
     * 
     */
    public RegisterUserRequest createRegisterUserRequest() {
        return new RegisterUserRequest();
    }

    /**
     * Create an instance of {@link CreateAccountRequest }
     * 
     */
    public CreateAccountRequest createCreateAccountRequest() {
        return new CreateAccountRequest();
    }

    /**
     * Create an instance of {@link ServiceFault }
     * 
     */
    public ServiceFault createServiceFault() {
        return new ServiceFault();
    }

    /**
     * Create an instance of {@link GetCountryRequest }
     * 
     */
    public GetCountryRequest createGetCountryRequest() {
        return new GetCountryRequest();
    }

    /**
     * Create an instance of {@link GetBalanceResponse }
     * 
     */
    public GetBalanceResponse createGetBalanceResponse() {
        return new GetBalanceResponse();
    }

    /**
     * Create an instance of {@link Balance }
     * 
     */
    public Balance createBalance() {
        return new Balance();
    }

    /**
     * Create an instance of {@link GetCountryResponse }
     * 
     */
    public GetCountryResponse createGetCountryResponse() {
        return new GetCountryResponse();
    }

    /**
     * Create an instance of {@link Country }
     * 
     */
    public Country createCountry() {
        return new Country();
    }

    /**
     * Create an instance of {@link DepositMoneyRequest }
     * 
     */
    public DepositMoneyRequest createDepositMoneyRequest() {
        return new DepositMoneyRequest();
    }

    /**
     * Create an instance of {@link RegisterUserResponse }
     * 
     */
    public RegisterUserResponse createRegisterUserResponse() {
        return new RegisterUserResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MyHeaders }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://spring.io/guides/gs-producing-web-service", name = "myHeaders")
    public JAXBElement<MyHeaders> createMyHeaders(MyHeaders value) {
        return new JAXBElement<MyHeaders>(_MyHeaders_QNAME, MyHeaders.class, null, value);
    }

}
