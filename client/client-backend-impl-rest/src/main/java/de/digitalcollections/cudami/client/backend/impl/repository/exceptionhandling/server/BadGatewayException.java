package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server;

import feign.Response;

public class BadGatewayException extends HttpServerException {

  public BadGatewayException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
