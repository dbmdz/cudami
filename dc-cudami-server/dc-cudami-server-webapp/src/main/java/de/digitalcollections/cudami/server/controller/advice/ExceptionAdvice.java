package de.digitalcollections.cudami.server.controller.advice;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ResourceNotFoundException;
import de.digitalcollections.model.exception.Problem;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
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

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Problem> handleAllOther(Exception exception) {
    LOGGER.error("exception stack trace", exception);
    Throwable cause = exception;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    Problem problem =
        new Problem(
            cause.getClass().getSimpleName(), "Service Exception", 500, cause.getMessage(), null);
    return new ResponseEntity<>(problem, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ResponseStatus(HttpStatus.CONFLICT)
  @ExceptionHandler(ConflictException.class)
  public void handleConflict(ConflictException exception) {}

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public void handleHttpMediaTypeNotAcceptableException() {}

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(value = {ResourceNotFoundException.class, UsernameNotFoundException.class})
  public void handleNotFound() {}

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Object> handleValidationException(ValidationException exception) {
    Map<String, Object> body = new LinkedHashMap<>(2);
    body.put("timestamp", new Date());
    body.put("errors", exception.getErrors());

    return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
  }
}
