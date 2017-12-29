package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.cudami.model.api.identifiable.ContentNode;
import java.util.List;

/**
 * A conent tree.
 */
public interface ContentTree extends Entity {

  List<ContentNode> getRootNodes();

  void setRootNodes(List<ContentNode> rootNodes);
}
