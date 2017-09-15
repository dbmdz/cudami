package de.digitalcollections.cudami.template.website.springboot.repository.exceptionhandling.server;

import de.digitalcollections.cudami.template.website.springboot.repository.exceptionhandling.HttpException;
import feign.Response;

public class HttpServerException extends HttpException {

  public HttpServerException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
