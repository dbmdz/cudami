package de.digitalcollections.cudami.lobid.client.exceptions.client;

public class ResourceNotFoundException extends HttpClientException {

  public ResourceNotFoundException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
