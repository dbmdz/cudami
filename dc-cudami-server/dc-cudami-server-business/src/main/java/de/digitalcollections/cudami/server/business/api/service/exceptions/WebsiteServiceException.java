package de.digitalcollections.cudami.server.business.api.service.exceptions;

public class WebsiteServiceException extends Exception {

  public WebsiteServiceException(String message) {
    super(message);
  }

  public WebsiteServiceException(String message, Throwable cause) {
    super(message, cause);
  }
}
