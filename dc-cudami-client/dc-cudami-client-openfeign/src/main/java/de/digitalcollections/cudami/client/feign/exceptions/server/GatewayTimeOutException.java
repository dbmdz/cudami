package de.digitalcollections.cudami.client.feign.exceptions.server;

import feign.Response;

public class GatewayTimeOutException extends HttpServerException {

  public GatewayTimeOutException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
