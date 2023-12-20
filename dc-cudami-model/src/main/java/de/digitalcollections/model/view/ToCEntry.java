package de.digitalcollections.model.view;

import java.util.ArrayList;
import java.util.List;

/** A table of contents entry (node in hierarchical "tree") */
public class ToCEntry {

  private List<ToCEntry> children;
  private String label;
  private ToCEntry parent;
  private String targetId;

  public void addChild(ToCEntry child) {
    if (getChildren() == null) {
      setChildren(new ArrayList<>(0));
    }
    getChildren().add(child);
  }

  public List<ToCEntry> getChildren() {
    return children;
  }

  public String getLabel() {
    return label;
  }

  public ToCEntry getParent() {
    return parent;
  }

  public String getTargetId() {
    return targetId;
  }

  public void setChildren(List<ToCEntry> children) {
    this.children = children;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setParent(ToCEntry parent) {
    this.parent = parent;
  }

  public void setTargetId(String targetId) {
    this.targetId = targetId;
  }
}
