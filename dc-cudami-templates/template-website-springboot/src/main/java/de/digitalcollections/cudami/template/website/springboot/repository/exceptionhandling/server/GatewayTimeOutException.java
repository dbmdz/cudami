package de.digitalcollections.cudami.template.website.springboot.repository.exceptionhandling.server;

import feign.Response;

public class GatewayTimeOutException extends HttpServerException {

  public GatewayTimeOutException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
