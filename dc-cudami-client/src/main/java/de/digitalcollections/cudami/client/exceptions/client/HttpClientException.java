package de.digitalcollections.cudami.client.exceptions.client;

import de.digitalcollections.cudami.client.exceptions.HttpException;
import feign.Response;

public class HttpClientException extends HttpException {

  public HttpClientException(String methodKey, Response response) {
    super(methodKey, response.status());
  }
}
