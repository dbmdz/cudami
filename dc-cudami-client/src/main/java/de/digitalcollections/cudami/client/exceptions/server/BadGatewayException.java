package de.digitalcollections.cudami.client.exceptions.server;

import feign.Response;

public class BadGatewayException extends HttpServerException {

  public BadGatewayException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
