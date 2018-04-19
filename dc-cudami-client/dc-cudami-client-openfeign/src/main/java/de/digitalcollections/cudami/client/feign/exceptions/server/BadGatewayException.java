package de.digitalcollections.cudami.client.feign.exceptions.server;

import feign.Response;

public class BadGatewayException extends HttpServerException {

  public BadGatewayException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
