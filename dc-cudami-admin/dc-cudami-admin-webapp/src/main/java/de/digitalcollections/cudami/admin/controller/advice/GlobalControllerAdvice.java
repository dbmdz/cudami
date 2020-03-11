package de.digitalcollections.cudami.admin.controller.advice;

import de.digitalcollections.cudami.admin.config.WebjarProperties;
import java.util.Map;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Adds the webjar versions read from application.yml as global model attribute. */
@ControllerAdvice
public class GlobalControllerAdvice {

  private final Map<String, String> webjarVersions;

  public GlobalControllerAdvice(WebjarProperties webjarProperties) {
    this.webjarVersions = webjarProperties.getVersions();
  }

  @ModelAttribute("webjarVersions")
  public Map<String, String> getWebjarVersions() {
    return webjarVersions;
  }
}
