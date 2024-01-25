package de.digitalcollections.model.view;

import de.digitalcollections.model.text.LocalizedText;

/** Single node in the list of a breadcrumb navigation */
public class BreadcrumbNode {

  private LocalizedText label;
  private String targetId;

  public LocalizedText getLabel() {
    return label;
  }

  public String getTargetId() {
    return targetId;
  }

  public void setLabel(LocalizedText label) {
    this.label = label;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }
}
