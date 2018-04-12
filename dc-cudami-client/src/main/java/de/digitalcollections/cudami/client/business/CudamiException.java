package de.digitalcollections.cudami.client.business;

public class CudamiException extends Exception {

  public CudamiException(String message) {
    super(message);
  }

  public CudamiException(String message, Throwable cause) {
    super(message, cause);
  }
}
