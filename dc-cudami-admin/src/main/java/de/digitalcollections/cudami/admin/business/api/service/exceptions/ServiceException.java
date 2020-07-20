package de.digitalcollections.cudami.admin.business.api.service.exceptions;

public class ServiceException extends Exception {

  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
