package de.digitalcollections.model.view;

import lombok.experimental.SuperBuilder;

/** Contains hints for rendering a webpage */
@SuperBuilder
public class RenderingHints {

  /** Defines if an in-page navigation (a TOC) is rendered */
  private boolean showInPageNavigation;

  /** Defines the name of the template to use for rendering */
  private String templateName;

  public RenderingHints() {}

  public RenderingHints(boolean showInPageNavigation, String templateName) {
    this.showInPageNavigation = showInPageNavigation;
    this.templateName = templateName;
  }

  public String getTemplateName() {
    return templateName;
  }

  public boolean isShowInPageNavigation() {
    return showInPageNavigation;
  }

  public void setShowInPageNavigation(boolean showInPageNavigation) {
    this.showInPageNavigation = showInPageNavigation;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }
}
