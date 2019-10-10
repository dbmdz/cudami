package de.digitalcollections.cudami.client.exceptions.server;

import feign.Response;

public class NotImplementedException extends HttpServerException {

  public NotImplementedException(String methodKey, Response response) {
    super(methodKey, response);
  }
}
