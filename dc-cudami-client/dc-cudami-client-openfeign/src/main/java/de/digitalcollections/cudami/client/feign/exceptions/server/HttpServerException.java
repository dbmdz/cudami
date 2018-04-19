package de.digitalcollections.cudami.client.feign.exceptions.server;

import de.digitalcollections.cudami.client.feign.exceptions.HttpException;
import feign.Response;

public class HttpServerException extends HttpException {

  public HttpServerException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
