package de.digitalcollections.cudami.model.impl.entity;

import de.digitalcollections.cudami.model.api.entity.ContentTree;
import de.digitalcollections.cudami.model.api.enums.EntityType;
import de.digitalcollections.cudami.model.api.identifiable.ContentNode;
import java.util.List;

/**
 * see {@link ContentTree}
 */
public class ContentTreeImpl extends EntityImpl implements ContentTree {

  private List<ContentNode> rootNodes;

  public ContentTreeImpl() {
    this.entityType = EntityType.CONTENT_TREE;
  }

  public ContentTreeImpl(List<ContentNode> rootNodes) {
    this();
    this.rootNodes = rootNodes;
  }

  @Override
  public List<ContentNode> getRootNodes() {
    return rootNodes;
  }

  @Override
  public void setRootNodes(List<ContentNode> rootNodes) {
    this.rootNodes = rootNodes;
  }

}
