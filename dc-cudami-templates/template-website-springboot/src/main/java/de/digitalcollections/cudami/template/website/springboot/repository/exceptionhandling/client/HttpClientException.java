package de.digitalcollections.cudami.template.website.springboot.repository.exceptionhandling.client;

import de.digitalcollections.cudami.template.website.springboot.repository.exceptionhandling.HttpException;
import feign.Response;

public class HttpClientException extends HttpException {

  public HttpClientException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
