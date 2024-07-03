package io.github.dbmdz.cudami.controller.advice;

import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.security.Role;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.dbmdz.cudami.propertyeditor.RoleEditor;
import java.sql.Timestamp;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Adds the webapp version read from application.yml as global model attribute and initializes
 * custom property editors.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

  private final RoleEditor roleEditor;
  private final String version;
  private final String activeProfile;
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerAdvice.class);

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public GlobalControllerAdvice(
      RoleEditor roleEditor,
      @Value("${info.app.project.version}") String version,
      Environment environment) {
    this.roleEditor = roleEditor;
    this.version = version;
    String[] activeProfiles = environment.getActiveProfiles();
    if (activeProfiles.length == 1) {
      activeProfile = activeProfiles[0];
    } else {
      activeProfile = "unknown";
    }
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleResourceNotFoundException() {
    /*
    unsuitable for REST APIs. For such cases it is
    preferable to use a ResponseEntity as
    a return type and avoid the use of @ResponseStatus altogether.
    */
    return "error/404";
  }

  @ExceptionHandler(value = {Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ModelAndView handleAllException(
      Exception e, HandlerMethod handlerMethod, HttpServletRequest request) {
    LOGGER.error("Internal Error: " + e, e);
    ModelAndView model = new ModelAndView("error/500");
    model.addObject("errorCode", "500");
    // if application is not running in production, we can add stacktrace for further information
    if (!"PROD".equals(activeProfile)) {
      model.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR);
      model.addObject("error", e.getCause());
      model.addObject("message", e.getMessage());
      model.addObject("stacktrace", e.getStackTrace());
      model.addObject("timestamp", new Timestamp(new Date().getTime()));
      model.addObject("exception", e);
      model.addObject("path", request.getRequestURL().toString());
    }
    return model;
  }

  //  @ExceptionHandler(ResourceNotFoundException.class)
  //  public ResponseEntity handleResourceNotFoundException(Model model) {
  //    return ResponseEntity.notFound().build();
  //  }

  @InitBinder
  public void registerCustomEditors(WebDataBinder binder, WebRequest request) {
    binder.registerCustomEditor(Role.class, roleEditor);
    binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
  }

  @ModelAttribute("version")
  public String getVersion() {
    return version;
  }
}
