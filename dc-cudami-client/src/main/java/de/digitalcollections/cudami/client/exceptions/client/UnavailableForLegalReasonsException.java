package de.digitalcollections.cudami.client.exceptions.client;

public class UnavailableForLegalReasonsException extends HttpClientException {

  public UnavailableForLegalReasonsException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
