package de.digitalcollections.cudami.lobid.client.exceptions.server;

public class NotImplementedException extends HttpServerException {

  public NotImplementedException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
