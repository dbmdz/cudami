package de.digitalcollections.cudami.lobid.client.exceptions.client;

public class ResourceException extends HttpClientException {

  public ResourceException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
