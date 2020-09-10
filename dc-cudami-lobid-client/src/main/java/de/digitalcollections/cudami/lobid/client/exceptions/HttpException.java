package de.digitalcollections.cudami.lobid.client.exceptions;

public class HttpException extends Exception {

  private final int statuscode;

  public HttpException(String methodKey, int statusCode) {
    super(String.format("Got status code %d for backend call %s.", statusCode, methodKey));
    this.statuscode = statusCode;
  }

  public HttpException(String methodKey, Exception ex) {
    super(String.format("Got exception for backend call %s.", methodKey), ex);
    this.statuscode = -1;
  }

  public int getStatusCode() {
    return statuscode;
  }
}
