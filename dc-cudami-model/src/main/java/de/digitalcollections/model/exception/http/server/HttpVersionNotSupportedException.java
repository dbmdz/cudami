package de.digitalcollections.model.exception.http.server;

public class HttpVersionNotSupportedException extends HttpServerException {

  public HttpVersionNotSupportedException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
