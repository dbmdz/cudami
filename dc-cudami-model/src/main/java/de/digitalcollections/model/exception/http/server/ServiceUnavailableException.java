package de.digitalcollections.model.exception.http.server;

public class ServiceUnavailableException extends HttpServerException {

  public ServiceUnavailableException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
