package de.digitalcollections.cudami.client.feign.exceptions.client;

import de.digitalcollections.cudami.client.feign.exceptions.HttpException;
import feign.Response;

public class HttpClientException extends HttpException {

  public HttpClientException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
