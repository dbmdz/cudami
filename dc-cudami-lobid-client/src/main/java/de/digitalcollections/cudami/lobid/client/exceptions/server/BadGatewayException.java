package de.digitalcollections.cudami.lobid.client.exceptions.server;

public class BadGatewayException extends HttpServerException {

  public BadGatewayException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
