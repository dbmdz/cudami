package de.digitalcollections.cudami.lobid.client.exceptions.server;

public class ServiceUnavailableException extends HttpServerException {

  public ServiceUnavailableException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
