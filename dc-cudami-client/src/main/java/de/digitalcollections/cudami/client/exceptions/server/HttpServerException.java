package de.digitalcollections.cudami.client.exceptions.server;

import de.digitalcollections.cudami.client.exceptions.HttpException;

public class HttpServerException extends HttpException {

  public HttpServerException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
