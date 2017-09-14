package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server;

import feign.Response;

public class ServiceUnavailableException extends HttpServerException {

  public ServiceUnavailableException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
