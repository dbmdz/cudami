package de.digitalcollections.model.exception.http.client;

public class UnavailableForLegalReasonsException extends HttpClientException {

  public UnavailableForLegalReasonsException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
