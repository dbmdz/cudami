package de.digitalcollections.model.exception.http.client;

public class UnprocessableEntityException extends HttpClientException {

  public UnprocessableEntityException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
