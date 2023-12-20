package de.digitalcollections.model.exception.http.server;

public class NotImplementedException extends HttpServerException {

  public NotImplementedException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
