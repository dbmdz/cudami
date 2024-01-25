package de.digitalcollections.model.exception.http.server;

public class GatewayTimeOutException extends HttpServerException {

  public GatewayTimeOutException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
