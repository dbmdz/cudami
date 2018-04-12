package de.digitalcollections.cudami.client.rest.exceptions.client;

import feign.Response;

public class ResourceException extends HttpClientException {

  public ResourceException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
