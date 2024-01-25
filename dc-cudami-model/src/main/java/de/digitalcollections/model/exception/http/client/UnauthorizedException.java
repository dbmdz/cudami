package de.digitalcollections.model.exception.http.client;

public class UnauthorizedException extends HttpClientException {

  public UnauthorizedException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
