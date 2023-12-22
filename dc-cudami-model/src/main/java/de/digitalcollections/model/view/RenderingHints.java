package de.digitalcollections.model.view;

import java.util.Objects;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RenderingHints)) return false;
    RenderingHints that = (RenderingHints) o;
    return showInPageNavigation == that.showInPageNavigation
        && Objects.equals(templateName, that.templateName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(showInPageNavigation, templateName);
  }

  @Override
  public String toString() {
    return "RenderingHints{"
        + "showInPageNavigation="
        + showInPageNavigation
        + ", templateName='"
        + templateName
        + '\''
        + '}';
  }
}
