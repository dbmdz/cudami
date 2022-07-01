package de.digitalcollections.cudami.server.controller.advice;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionAdvice {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAdvice.class);

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public void handleAllOther(Exception exception) {
    LOGGER.error("exception stack trace", exception);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ConflictException.class)
  public void handleConflict(ConflictException exception) {}

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public void handleHttpMediaTypeNotAcceptableException() {}

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(UsernameNotFoundException.class)
  public void handleNotFound() {}

  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(ValidationException.class)
  public void handleValidationException(ValidationException exception) {
    LOGGER.error("Validation error: ", exception);
  }
}
