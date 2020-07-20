package de.digitalcollections.cudami.client.exceptions.client;

public class ResourceException extends HttpClientException {

  public ResourceException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
