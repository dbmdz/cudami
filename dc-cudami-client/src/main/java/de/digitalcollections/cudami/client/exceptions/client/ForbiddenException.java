package de.digitalcollections.cudami.client.exceptions.client;

public class ForbiddenException extends HttpClientException {

  public ForbiddenException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
