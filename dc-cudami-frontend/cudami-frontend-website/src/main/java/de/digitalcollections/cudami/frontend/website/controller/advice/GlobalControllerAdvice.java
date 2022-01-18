package de.digitalcollections.cudami.frontend.website.controller.advice;

import de.digitalcollections.cudami.frontend.website.config.TemplateConfig;
import de.digitalcollections.cudami.frontend.website.service.ContentService;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.List;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

@ControllerAdvice
@SessionAttributes("maxLevel")
public class GlobalControllerAdvice {

  private final ContentService contentService;
  private final TemplateConfig templateConfig;

  public GlobalControllerAdvice(TemplateConfig templateConfig, ContentService contentService) {
    this.templateConfig = templateConfig;
    this.contentService = contentService;
  }

  @ModelAttribute("contentWebpages")
  public List<Webpage> getContentWebpages() {
    return contentService.getContentPages();
  }

  @ModelAttribute("footerWebpages")
  public List<Webpage> getFooterWebpages() {
    return contentService.getFooterPages();
  }

  @ModelAttribute("navMaxLevel")
  public int getTemplateNavMaxLevel() {
    return templateConfig.getNavMaxLevel();
  }

  @ModelAttribute("website")
  public Website getWebsite() {
    return contentService.getWebsite();
  }
}
