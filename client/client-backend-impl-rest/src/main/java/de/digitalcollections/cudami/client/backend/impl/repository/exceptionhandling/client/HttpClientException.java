package de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.client;

import de.digitalcollections.cudami.client.backend.impl.repository.exceptionhandling.HttpException;
import feign.Response;

public class HttpClientException extends HttpException {

  public HttpClientException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
