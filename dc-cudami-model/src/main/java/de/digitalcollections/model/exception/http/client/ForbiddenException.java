package de.digitalcollections.model.exception.http.client;

public class ForbiddenException extends HttpClientException {

  public ForbiddenException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
