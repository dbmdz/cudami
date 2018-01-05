package de.digitalcollections.cudami.model.impl.identifiable.entity;

import de.digitalcollections.cudami.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.entity.EntityType;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import java.util.List;

/**
 * see {@link ContentTree}
 */
public class ContentTreeImpl extends EntityImpl implements ContentTree {

  private List<ContentNode> rootNodes;

  public ContentTreeImpl() {
    super();
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
