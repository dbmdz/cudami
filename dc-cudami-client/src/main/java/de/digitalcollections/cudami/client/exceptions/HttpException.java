package de.digitalcollections.cudami.client.exceptions;

public class HttpException extends Exception {

  private final int statuscode;

  public HttpException(String methodKey, int statusCode) {
    super(String.format("Got %d for backend call %s.", statusCode, methodKey));
    this.statuscode = statusCode;
  }

  public int getStatusCode() {
    return statuscode;
  }
}
