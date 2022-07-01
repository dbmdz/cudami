package de.digitalcollections.cudami.server.business.api.service.exceptions;

public class CudamiServiceException extends Exception {

  public CudamiServiceException(String message) {
    super(message);
  }

  public CudamiServiceException(Throwable cause) {
    super("An unexpected error occured!", cause);
  }

  public CudamiServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
