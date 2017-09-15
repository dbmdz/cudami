package de.digitalcollections.cudami.template.website.springboot.repository.exceptionhandling.server;

import feign.Response;

public class ServiceUnavailableException extends HttpServerException {

  public ServiceUnavailableException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
