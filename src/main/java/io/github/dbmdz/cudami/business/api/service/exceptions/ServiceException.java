package io.github.dbmdz.cudami.business.api.service.exceptions;

public class ServiceException extends Exception {

  public ServiceException(String message) {
    super(message);
  }

  public ServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
