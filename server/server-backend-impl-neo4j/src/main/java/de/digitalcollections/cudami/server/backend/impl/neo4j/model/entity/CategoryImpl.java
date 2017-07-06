package de.digitalcollections.cudami.server.backend.impl.neo4j.model.entity;

import de.digitalcollections.cudami.model.api.Text;
import de.digitalcollections.cudami.model.api.entity.BinaryContent;
import java.util.ArrayList;
import java.util.List;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import de.digitalcollections.cudami.model.api.entity.ContentNode;
import de.digitalcollections.cudami.model.api.entity.IiifContent;
import de.digitalcollections.cudami.model.api.entity.TextContent;

@NodeEntity(label = "Category")
public class CategoryImpl extends EntityImpl implements ContentNode<Long> {

  List<ContentNode> children;

  @Relationship(type = "HAS_LABEL")
  private Text label;

  @Relationship(type = "HAS_PARENT")
  ContentNode parent;

  public CategoryImpl(Text label) {
    this.label = label;
  }

  @Override
  public void addChild(ContentNode category) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(category);
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
  public Text getLabel() {
    return label;
  }

  @Override
  public void setLabel(Text label) {
    this.label = label;
  }

  @Override
  public ContentNode getParent() {
    return parent;
  }

  @Override
  public void setParent(ContentNode parent) {
    this.parent = parent;
  }

  @Override
  public List<TextContent> getTextContents() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setTextContents(List<TextContent> textContents) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<BinaryContent> getBinaryContents() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setBinaryContents(List<BinaryContent> binaryContents) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List<IiifContent> getIiifContents() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setIiifContents(List<IiifContent> iiifContents) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
