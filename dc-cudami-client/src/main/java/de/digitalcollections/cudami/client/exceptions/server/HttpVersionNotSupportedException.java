package de.digitalcollections.cudami.client.exceptions.server;

public class HttpVersionNotSupportedException extends HttpServerException {

  public HttpVersionNotSupportedException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
