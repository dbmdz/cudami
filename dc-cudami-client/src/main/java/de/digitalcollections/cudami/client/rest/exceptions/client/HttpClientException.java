package de.digitalcollections.cudami.client.rest.exceptions.client;

import de.digitalcollections.cudami.client.rest.exceptions.HttpException;
import feign.Response;

public class HttpClientException extends HttpException {

  public HttpClientException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
