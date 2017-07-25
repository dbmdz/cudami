package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server;

import feign.Response;

public class GatewayTimeOutException extends HttpServerException {

  public GatewayTimeOutException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
