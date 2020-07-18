package de.digitalcollections.cudami.client.exceptions.client;

public class ResourceNotFoundException extends HttpClientException {

  public ResourceNotFoundException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
