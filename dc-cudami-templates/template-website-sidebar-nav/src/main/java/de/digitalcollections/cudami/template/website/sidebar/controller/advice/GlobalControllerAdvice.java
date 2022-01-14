package de.digitalcollections.cudami.template.website.sidebar.controller.advice;

import de.digitalcollections.cudami.template.website.sidebar.service.ContentService;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.List;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

  private final ContentService contentService;

  public GlobalControllerAdvice(ContentService contentService) {
    this.contentService = contentService;
  }

  @ModelAttribute("rootWebpages")
  public List<Webpage> getRootWebpages() {
    return contentService.getSitemap();
  }

  @ModelAttribute("website")
  public Website getWebsite() {
    return contentService.getWebsite();
  }
}
