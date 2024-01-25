package de.digitalcollections.cudami.admin.controller;

import de.digitalcollections.model.exception.ResourceNotFoundException;
import java.sql.Timestamp;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/** Global exception handling. */
@ControllerAdvice
public class GlobalExceptionController implements EnvironmentAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionController.class);
  private String activeProfile;

  @Override
  public void setEnvironment(Environment environment) {
    String[] activeProfiles = environment.getActiveProfiles();
    if (activeProfiles.length == 1) {
      activeProfile = activeProfiles[0];
    }
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ModelAndView handleResourceNotFoundException(Exception ex) {
    ModelAndView model = new ModelAndView("error");
    model.addObject("timestamp", new Timestamp(new Date().getTime()));
    model.addObject("errorCode", "404");

    return model;
  }

  @ExceptionHandler(value = {Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ModelAndView handleAllException(Exception ex) {
    LOGGER.error("Internal Error", ex);
    ModelAndView model = new ModelAndView("error");
    model.addObject("timestamp", new Timestamp(new Date().getTime()));
    model.addObject("errorCode", "500");
    // if application is not running in production, we can add stacktrace for further information
    if (!"PROD".equals(activeProfile)) {
      model.addObject("exception", ex);
    }
    return model;
  }
}
