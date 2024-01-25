package de.digitalcollections.model.exception.http.server;

public class BadGatewayException extends HttpServerException {

  public BadGatewayException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
