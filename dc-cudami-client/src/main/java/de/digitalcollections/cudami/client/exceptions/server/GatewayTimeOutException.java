package de.digitalcollections.cudami.client.exceptions.server;

public class GatewayTimeOutException extends HttpServerException {

  public GatewayTimeOutException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
