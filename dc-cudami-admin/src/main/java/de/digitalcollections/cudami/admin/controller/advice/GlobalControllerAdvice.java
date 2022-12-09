package de.digitalcollections.cudami.admin.controller.advice;

import de.digitalcollections.cudami.admin.propertyeditor.RoleEditor;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.security.Role;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

/**
 * Adds the webapp version read from application.yml as global model attribute and initializes
 * custom property editors.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

  private final RoleEditor roleEditor;
  private final String version;

  @SuppressFBWarnings(value = "EI_EXPOSE_REP2")
  public GlobalControllerAdvice(
      RoleEditor roleEditor, @Value("${info.app.project.version}") String version) {
    this.roleEditor = roleEditor;
    this.version = version;
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleResourceNotFoundException(Model model) {
    /*
    unsuitable for REST APIs. For such cases it is
    preferable to use a ResponseEntity as
    a return type and avoid the use of @ResponseStatus altogether.
    */
    return "error/404";
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
