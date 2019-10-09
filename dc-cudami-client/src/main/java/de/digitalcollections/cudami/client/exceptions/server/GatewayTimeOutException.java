package de.digitalcollections.cudami.client.exceptions.server;

import feign.Response;

public class GatewayTimeOutException extends HttpServerException {

  public GatewayTimeOutException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
