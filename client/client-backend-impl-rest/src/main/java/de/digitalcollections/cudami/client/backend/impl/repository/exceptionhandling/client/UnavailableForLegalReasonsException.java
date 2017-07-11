package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client;

import feign.Response;

public class UnavailableForLegalReasonsException extends HttpClientException {

  public UnavailableForLegalReasonsException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
