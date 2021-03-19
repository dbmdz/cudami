package de.digitalcollections.cudami.admin.controller.advice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds the webjar versions read from application.yml as global model attribute. */
@ControllerAdvice
public class GlobalControllerAdvice {

  private final String version;

  public GlobalControllerAdvice(@Value("${info.app.project.version}") String version) {
    this.version = version;
  }

  @ModelAttribute("version")
  public String getVersion() {
    return version;
  }
}
