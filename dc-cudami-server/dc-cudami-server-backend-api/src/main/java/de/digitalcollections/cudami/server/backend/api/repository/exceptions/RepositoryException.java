package de.digitalcollections.cudami.server.backend.api.repository.exceptions;

public class RepositoryException extends Exception {

  public RepositoryException(String message) {
    super(message);
  }

  public RepositoryException(Throwable cause) {
    super("An unexpected error occured!", cause);
  }

  public RepositoryException(String message, Throwable cause) {
    super(message, cause);
  }
}
