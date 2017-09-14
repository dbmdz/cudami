package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server;

import feign.Response;

public class NotImplementedException extends HttpServerException {

  public NotImplementedException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
