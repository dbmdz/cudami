package de.digitalcollections.cudami.admin.business.api.service.exceptions;

public class EntityServiceException extends Exception {

  public EntityServiceException(String message) {
    super(message);
  }

  public EntityServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
