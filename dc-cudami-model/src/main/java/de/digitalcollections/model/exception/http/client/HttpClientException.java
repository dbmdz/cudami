package de.digitalcollections.model.exception.http.client;

import de.digitalcollections.model.exception.http.HttpException;

public class HttpClientException extends HttpException {

  public HttpClientException(String methodKey, int status, String request) {
    super(methodKey, status, request);
  }
}
