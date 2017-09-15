package de.digitalcollections.cudami.template.website.springboot.repository.exceptionhandling;

import feign.Response;

public class HttpException extends RuntimeException {

  public HttpException(String methodKey, Response response) {
    super(String.format("Got %d for backend call %s.%n⤷ %s",
             response.status(), methodKey, response.request()));
  }

}
