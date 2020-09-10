package de.digitalcollections.cudami.lobid.client.exceptions.server;

import de.digitalcollections.cudami.lobid.client.exceptions.HttpException;

public class HttpServerException extends HttpException {

  public HttpServerException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
