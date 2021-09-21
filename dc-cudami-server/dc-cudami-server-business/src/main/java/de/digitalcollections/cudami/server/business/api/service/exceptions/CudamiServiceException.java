package de.digitalcollections.cudami.server.business.api.service.exceptions;

public class CudamiServiceException extends Exception {

  public CudamiServiceException(String message) {
    super(message);
  }

  public CudamiServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
