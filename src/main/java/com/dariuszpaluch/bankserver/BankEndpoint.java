package com.dariuszpaluch.bankserver;

import io.spring.guides.gs_producing_web_service.GetBalanceRequest;
import io.spring.guides.gs_producing_web_service.GetBalanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

/**
 * Created by Dariusz Paluch on 06.01.2018.
 */
@Endpoint
public class BankEndpoint {
  private static final String NAMESPACE_URI = "http://spring.io/guides/gs-producing-web-service";

  private BankRepository bankRepository;

  @Autowired
  public BankEndpoint(BankRepository bankRepository) {
    this.bankRepository = bankRepository;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getBalanceRequest")
  @ResponsePayload
  public GetBalanceResponse getBalance(@RequestPayload GetBalanceRequest request) {
    GetBalanceResponse response = new GetBalanceResponse();
    response.setBalance(bankRepository.getBalance(request.getAccountNo()));

    return response;
  }
}
