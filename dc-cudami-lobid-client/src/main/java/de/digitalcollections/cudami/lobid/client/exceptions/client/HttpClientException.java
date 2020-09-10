package de.digitalcollections.cudami.lobid.client.exceptions.client;

import de.digitalcollections.cudami.lobid.client.exceptions.HttpException;

public class HttpClientException extends HttpException {

  public HttpClientException(String methodKey, int statusCode) {
    super(methodKey, statusCode);
  }
}
