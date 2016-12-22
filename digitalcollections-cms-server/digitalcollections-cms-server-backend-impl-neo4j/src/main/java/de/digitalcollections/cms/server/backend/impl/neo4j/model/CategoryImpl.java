package de.digitalcollections.cms.server.backend.impl.neo4j.model;

import de.digitalcollections.cms.model.api.Category;
import de.digitalcollections.cms.model.api.Text;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity(label = "Category")
public class CategoryImpl extends EntityImpl implements Category {

  @Relationship(type = "HAS_LABEL")
  private Text label;

  public CategoryImpl(Text label) {
    this.label = label;
  }

  @Override
  public Text getLabel() {
    return label;
  }

  @Override
  public void setLabel(Text label) {
    this.label = label;
  }
}
