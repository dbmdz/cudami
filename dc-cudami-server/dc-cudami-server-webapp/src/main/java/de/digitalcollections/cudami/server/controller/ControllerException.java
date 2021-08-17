package de.digitalcollections.cudami.server.controller;

public class ControllerException extends Exception {

  public ControllerException(Throwable cause) {
    super(cause);
  }

  public ControllerException(String message, Throwable cause) {
    super(message, cause);
  }
}
