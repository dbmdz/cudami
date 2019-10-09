package de.digitalcollections.cudami.client.exceptions.server;

import feign.Response;

public class HttpVersionNotSupportedException extends HttpServerException {

  public HttpVersionNotSupportedException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
