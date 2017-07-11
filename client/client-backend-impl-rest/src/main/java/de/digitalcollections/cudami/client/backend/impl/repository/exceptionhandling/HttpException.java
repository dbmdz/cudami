package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling;

import feign.Response;

public class HttpException extends RuntimeException {

  public HttpException(String methodKey, Response response) {
    super(String.format("Got %d for backend call %s.%n⤷ %s",
             response.status(), methodKey, response.request()));
  }

}
