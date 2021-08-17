package de.digitalcollections.cudami.server.business.api.service.exceptions;

public class EntityServiceException extends CudamiServiceException {

  public EntityServiceException(String message) {
    super(message);
  }

  public EntityServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
