package de.digitalcollections.cudami.client.exceptions.server;

public class ServiceUnavailableException extends HttpServerException {

  public ServiceUnavailableException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
