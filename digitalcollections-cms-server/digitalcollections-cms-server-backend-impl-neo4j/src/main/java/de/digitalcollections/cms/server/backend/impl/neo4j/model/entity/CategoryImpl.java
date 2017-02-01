package de.digitalcollections.cms.server.backend.impl.neo4j.model.entity;

import de.digitalcollections.cms.model.api.Text;
import de.digitalcollections.cms.model.api.entity.Category;
import java.util.ArrayList;
import java.util.List;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label = "Category")
public class CategoryImpl extends EntityImpl implements Category {

  List<Category> children;

  @Relationship(type = "HAS_LABEL")
  private Text label;

  @Relationship(type = "HAS_PARENT")
  Category parent;

  public CategoryImpl(Text label) {
    this.label = label;
  }

  @Override
  public void addChild(Category category) {
    if (children == null) {
      children = new ArrayList<>();
    }
    children.add(category);
  }

  @Override
  public List<Category> getChildren() {
    return children;
  }

  @Override
  public void setChildren(List<Category> children) {
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
  public Category getParent() {
    return parent;
  }

  @Override
  public void setParent(Category parent) {
    this.parent = parent;
  }
}
