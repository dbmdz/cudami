package de.digitalcollections.cudami.server.controller.advice;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionAdvice {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAdvice.class);

  @ExceptionHandler(UsernameNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void handleUsernameNotFoundException() {}

  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void handleHttpMediaTypeNotAcceptableException() {}

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
  public void handleValidationException(Exception exception) {
    LOGGER.error("Cannot process request: ", exception);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<String> handleMissingServletRequestParameterException(Exception exception) {
    return ResponseEntity.badRequest().body(exception.getMessage());
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public void handleAllOther(Exception exception) {
    LOGGER.error("exception stack trace", exception);
  }
}
