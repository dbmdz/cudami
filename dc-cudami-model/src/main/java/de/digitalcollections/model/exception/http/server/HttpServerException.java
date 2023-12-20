package de.digitalcollections.model.exception.http.server;

import de.digitalcollections.model.exception.http.HttpException;

public class HttpServerException extends HttpException {

  public HttpServerException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
