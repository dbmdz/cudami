package de.digitalcollections.cudami.lobid.client.exceptions.client;

public class UnauthorizedException extends HttpClientException {

  public UnauthorizedException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
