package de.digitalcollections.cudami.client.exceptions.client;

import feign.Response;

public class ResourceNotFoundException extends HttpClientException {

  public ResourceNotFoundException(String methodKey, Response response) {
    super(methodKey, response);
  }
}
