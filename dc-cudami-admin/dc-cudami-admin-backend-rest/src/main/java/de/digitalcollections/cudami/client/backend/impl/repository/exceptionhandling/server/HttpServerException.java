package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.server;

import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.HttpException;
import feign.Response;

public class HttpServerException extends HttpException {

  public HttpServerException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
