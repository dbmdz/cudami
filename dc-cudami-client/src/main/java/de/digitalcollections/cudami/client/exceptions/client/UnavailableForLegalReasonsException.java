package de.digitalcollections.cudami.client.exceptions.client;

import feign.Response;

public class UnavailableForLegalReasonsException extends HttpClientException {

  public UnavailableForLegalReasonsException(String methodKey, Response response) {
    super(methodKey, response);
  }
}
