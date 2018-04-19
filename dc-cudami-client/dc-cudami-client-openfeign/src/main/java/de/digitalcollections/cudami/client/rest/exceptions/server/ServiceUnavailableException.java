package de.digitalcollections.cudami.client.rest.exceptions.server;

import feign.Response;

public class ServiceUnavailableException extends HttpServerException {

  public ServiceUnavailableException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
