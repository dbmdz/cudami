package de.digitalcollections.cudami.server.business.api.service.exceptions;

public class ResourceNotFoundException extends ServiceException {

  public ResourceNotFoundException(String message) {
    super(message);
  }
}
