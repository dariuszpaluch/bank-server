package com.dariuszpaluch.bankserver;

import com.dariuszpaluch.bankserver.utils.HeaderUtils;
import io.spring.guides.gs_producing_web_service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.server.endpoint.annotation.SoapHeader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.util.List;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
@Endpoint
public class BankSoapEndpoint {
  private static final String NAMESPACE_URI = "http://spring.io/guides/gs-producing-web-service";

  private BankRepository bankRepository;

  @Autowired
  public BankSoapEndpoint(BankRepository bankRepository) {
    this.bankRepository = bankRepository;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBalanceRequest")
  @ResponsePayload
  public GetBalanceResponse getBalance(@RequestPayload GetBalanceRequest request, @SoapHeader(
          value = "{http://spring.io/guides/gs-producing-web-service}myHeaders") SoapHeaderElement soapHeaderElement) {
    String userToken = HeaderUtils.getTokenFromHeader(soapHeaderElement);

    GetBalanceResponse response = new GetBalanceResponse();
    response.setBalance(bankRepository.getBalance(userToken, request.getAccountNo()));

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "depositMoneyRequest")
  @ResponsePayload
  public DepositMoneyResponse depositMoney(@RequestPayload DepositMoneyRequest request, @SoapHeader(
          value = "{http://spring.io/guides/gs-producing-web-service}myHeaders") SoapHeaderElement soapHeaderElement) {

    String userToken = HeaderUtils.getTokenFromHeader(soapHeaderElement);

    DepositMoneyResponse response = new DepositMoneyResponse();
    bankRepository.depositMoney(userToken, request.getAccountNo(), request.getAmount());
    response.setResult(true);
    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "withdrawMoneyRequest")
  @ResponsePayload
  public WithdrawMoneyResponse withdrawMoney(@RequestPayload WithdrawMoneyRequest request, @SoapHeader(
          value = "{http://spring.io/guides/gs-producing-web-service}myHeaders") SoapHeaderElement soapHeaderElement) throws Exception {
    WithdrawMoneyResponse response = new WithdrawMoneyResponse();
    response.setResult(bankRepository.withdrawMoney(HeaderUtils.getTokenFromHeader(soapHeaderElement), request.getAccountNo(), request.getAmount()));

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createAccountRequest")
  @ResponsePayload
  public CreateAccountResponse createAccount(@RequestPayload CreateAccountRequest request, @SoapHeader(
          value = "{http://spring.io/guides/gs-producing-web-service}myHeaders") SoapHeaderElement soapHeaderElement) throws Exception {
    CreateAccountResponse response = new CreateAccountResponse();
    response.setAccountNo(bankRepository.createAccount(HeaderUtils.getTokenFromHeader(soapHeaderElement)));

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "registerUserRequest")
  @ResponsePayload
  public RegisterUserResponse registerUser(@RequestPayload RegisterUserRequest request) {
    RegisterUserResponse response = new RegisterUserResponse();
    UserAuthenticateData userAuthenticateData = request.getUserAuthenticateData();
    response.setResult(bankRepository.registerUser(userAuthenticateData.getLogin(), userAuthenticateData.getPassword()));

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "authenticateRequest")
  @ResponsePayload
  public AuthenticateResponse authenticate(@RequestPayload AuthenticateRequest request) throws Exception {
    AuthenticateResponse response = new AuthenticateResponse();
    UserAuthenticateData userAuthenticateData = request.getUserAuthenticateData();
    response.setToken(bankRepository.authenticate(userAuthenticateData.getLogin(), userAuthenticateData.getPassword()));

    return response;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserAccountsRequest")
  @ResponsePayload
  public GetUserAccountsResponse getUserAccounts(@RequestPayload GetUserAccountsRequest request,  @SoapHeader(
          value = "{http://spring.io/guides/gs-producing-web-service}myHeaders") SoapHeaderElement soapHeaderElement) throws Exception {
    String userToken = HeaderUtils.getTokenFromHeader(soapHeaderElement);

    GetUserAccountsResponse response = new GetUserAccountsResponse();
    List<String> accounts = bankRepository.getUserAccounts(userToken);
    for(String accountNo : accounts) {
      response.getAccounts().add(accountNo);
    }

    return response;
  }
}
