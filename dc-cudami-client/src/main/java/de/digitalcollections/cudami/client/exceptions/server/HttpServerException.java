package de.digitalcollections.cudami.client.exceptions.server;

import de.digitalcollections.cudami.client.exceptions.HttpException;
import feign.Response;

public class HttpServerException extends HttpException {

  public HttpServerException(String methodKey, Response response) {
    super(methodKey, response.status());
  }
}
