package de.digitalcollections.cudami.client.rest.exceptions.client;

import feign.Response;

public class UnauthorizedException extends HttpClientException {

  public UnauthorizedException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
