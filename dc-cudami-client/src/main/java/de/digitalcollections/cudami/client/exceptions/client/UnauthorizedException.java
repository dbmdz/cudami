package de.digitalcollections.cudami.client.exceptions.client;

public class UnauthorizedException extends HttpClientException {

  public UnauthorizedException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
