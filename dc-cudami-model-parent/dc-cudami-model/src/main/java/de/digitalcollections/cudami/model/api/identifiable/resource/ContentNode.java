package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Node;
import java.util.List;

public interface ContentNode extends Resource, Node<ContentNode> {

  List<ContentNode> getSubNodes();

  void setSubNodes(List<ContentNode> subNodes);

}
