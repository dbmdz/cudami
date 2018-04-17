package de.digitalcollections.cudami.admin.business.api.service.exceptions;

public class IdentifiableServiceException extends Exception {

  public IdentifiableServiceException(String message) {
    super(message);
  }

  public IdentifiableServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
