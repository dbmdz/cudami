package de.digitalcollections.cudami.template.website.springboot.repository.exceptionhandling.client;

import feign.Response;

public class ForbiddenException extends HttpClientException {

  public ForbiddenException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
