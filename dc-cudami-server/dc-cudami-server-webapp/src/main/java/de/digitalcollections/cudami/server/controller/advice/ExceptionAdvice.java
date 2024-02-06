package de.digitalcollections.cudami.server.controller.advice;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ResourceNotFoundException;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
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
  @ExceptionHandler(value = {ResourceNotFoundException.class, UsernameNotFoundException.class})
  public void handleNotFound() {}

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<Object> handleValidationException(ValidationException exception) {
    Map<String, Object> body = new LinkedHashMap<>(3);
    body.put("timestamp", new Date());
    body.put("errors", exception.getErrors());
    if (StringUtils.hasText(exception.getMessage())) body.put("message", exception.getMessage());

    return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
  }
}
