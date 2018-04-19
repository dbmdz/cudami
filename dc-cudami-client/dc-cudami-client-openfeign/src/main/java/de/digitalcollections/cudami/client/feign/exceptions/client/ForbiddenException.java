package de.digitalcollections.cudami.client.feign.exceptions.client;

import feign.Response;

public class ForbiddenException extends HttpClientException {

  public ForbiddenException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
