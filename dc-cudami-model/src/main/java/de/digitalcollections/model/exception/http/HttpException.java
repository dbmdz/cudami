package de.digitalcollections.model.exception.http;

public class HttpException extends RuntimeException {

  private final String methodKey;
  private final String request;
  private final Integer statuscode;

  public HttpException(String methodKey, Exception ex) {
    super(String.format("Got exception for backend call %s.", methodKey), ex);
    this.methodKey = methodKey;
    this.request = null;
    this.statuscode = null;
  }

  public HttpException(String methodKey, int statuscode) {
    super(String.format("Got status code %d for backend call %s.", statuscode, methodKey));
    this.methodKey = methodKey;
    this.request = null;
    this.statuscode = statuscode;
  }

  public HttpException(String methodKey, int statuscode, String request) {
    super(String.format("Got %d for backend call %s.%n⤷ %s", statuscode, methodKey, request));
    this.methodKey = methodKey;
    this.request = request;
    this.statuscode = statuscode;
  }

  public String getMethodKey() {
    return methodKey;
  }

  public String getRequest() {
    return request;
  }

  public Integer getStatusCode() {
    return statuscode;
  }
}
