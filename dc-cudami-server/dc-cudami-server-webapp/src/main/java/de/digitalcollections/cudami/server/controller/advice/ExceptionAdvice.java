package de.digitalcollections.cudami.server.controller.advice;

import com.github.openjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionAdvice {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionAdvice.class);

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(UsernameNotFoundException.class)
  public void handleNotFound() {}

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public void handleHttpMediaTypeNotAcceptableException() {}

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(Exception.class)
  public void handleAllOther(Exception exception) {
    LOGGER.error("exception stack trace", exception);
  }

  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgumentException(
      IllegalArgumentException illegalArgumentException) {
    LOGGER.warn("Illegal argument: " + illegalArgumentException, illegalArgumentException);
    return new ResponseEntity<>(
        wrapInJsonError(illegalArgumentException.getMessage()), HttpStatus.BAD_REQUEST);
  }

  private String wrapInJsonError(String message) {
    return new JSONObject().put("error", message).toString();
  }
}
