package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client;

import feign.Response;

public class ResourceNotFoundException extends HttpClientException {

  public ResourceNotFoundException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
