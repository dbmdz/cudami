package de.digitalcollections.cudami.template.website.sidebar.controller.advice;

import de.digitalcollections.cudami.template.website.sidebar.config.TemplateConfig;
import de.digitalcollections.cudami.template.website.sidebar.service.ContentService;
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

  @ModelAttribute("footerWebpages")
  public List<Webpage> getFooterWebpages() {
    return contentService.getFooterPages();
  }

  @ModelAttribute("rootWebpages")
  public List<Webpage> getRootWebpages() {
    return contentService.getContentPages();
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
