package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.BinaryContent;
import de.digitalcollections.cudami.model.api.IiifContent;
import de.digitalcollections.cudami.model.api.entity.ContentNode;
import de.digitalcollections.cudami.model.api.entity.TextContent;
import java.util.List;

public class ContentNodeImpl extends EntityImpl implements ContentNode<Long> {

  private ContentNode parent;
  private List<ContentNode> children;
  private List<IiifContent> iiifContents;
  private List<TextContent> textContents;
  private List<BinaryContent> binaryContents;

  @Override
  public ContentNode getParent() {
    return parent;
  }

  @Override
  public void setParent(ContentNode parent) {
    this.parent = parent;
  }

  @Override
  public List<ContentNode> getChildren() {
    return children;
  }

  @Override
  public void setChildren(List<ContentNode> children) {
    this.children = children;
  }

  @Override
  public List<TextContent> getTextContents() {
    return textContents;
  }

  @Override
  public void setTextContents(List<TextContent> textContents) {
    this.textContents = textContents;
  }

  @Override
  public List<BinaryContent> getBinaryContents() {
    return binaryContents;
  }

  @Override
  public void setBinaryContents(List<BinaryContent> binaryContents) {
    this.binaryContents = binaryContents;
  }

  @Override
  public List<IiifContent> getIiifContents() {
    return iiifContents;
  }

  @Override
  public void setIiifContents(List<IiifContent> iiifContents) {
    this.iiifContents = iiifContents;
  }

}
