package de.digitalcollections.cudami.client.exceptions.client;

import de.digitalcollections.cudami.client.exceptions.HttpException;

public class HttpClientException extends HttpException {

  public HttpClientException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
