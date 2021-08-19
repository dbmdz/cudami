package de.digitalcollections.cudami.server.backend.api.repository.exceptions;

public class UrlAliasRepositoryException extends Exception {

  public UrlAliasRepositoryException(String message) {
    super(message);
  }

  public UrlAliasRepositoryException(Throwable cause) {
    super("An unexpected error occured!", cause);
  }

  public UrlAliasRepositoryException(String message, Throwable cause) {
    super(message, cause);
  }
}
