package de.digitalcollections.cudami.template.website.springboot.repository.exceptionhandling.client;

import feign.Response;

/**
 * HttpStatusCode 413 denoting the api is wrongfully using a teapot for making coffee as specified in the Hyper Text
 * Coffee Pot Control Protocoll (see <a href="https://tools.ietf.org/html/rfc2324">RFC 2324</a> for details).
 * 
 */
public class ImATeapotException extends HttpClientException {

  public ImATeapotException(String methodKey, Response response) {
    super(methodKey, response);
  }

}
