package de.digitalcollections.model.exception;

/** An exception indicating that there has been a technical error. */
public class TechnicalException extends Exception {

  public TechnicalException() {}

  public TechnicalException(String message) {
    super(message);
  }

  public TechnicalException(String message, Throwable cause) {
    super(message, cause);
  }

  public TechnicalException(Throwable cause) {
    super(cause);
  }
}
