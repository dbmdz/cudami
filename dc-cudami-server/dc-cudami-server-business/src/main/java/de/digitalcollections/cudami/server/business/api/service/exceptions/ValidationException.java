package de.digitalcollections.cudami.server.business.api.service.exceptions;

public class ValidationException extends Exception {

  public ValidationException(String msg, Exception e) {
    super(msg, e);
  }

  public ValidationException(String msg) {
    super(msg);
  }
}
