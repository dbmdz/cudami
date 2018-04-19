package de.digitalcollections.cudami.client.spring.business;

public class CudamiException extends Exception {

  public CudamiException(String message) {
    super(message);
  }

  public CudamiException(String message, Throwable cause) {
    super(message, cause);
  }
}
