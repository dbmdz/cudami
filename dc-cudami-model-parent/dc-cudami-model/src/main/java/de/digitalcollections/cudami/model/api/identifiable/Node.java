package de.digitalcollections.cudami.model.api.identifiable;

import de.digitalcollections.cudami.model.api.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * Node is used to structure cultural content hierarchically.
 */
public interface Node extends Identifiable {

  Text getLabel();

  void setLabel(Text label);

  Node getParent();

  void setParent(Node parent);

  List<Node> getChildren();

  void setChildren(List<Node> children);

  default void addChild(Node child) {
    if (getChildren() == null) {
      setChildren(new ArrayList<>());
    }
    getChildren().add(child);
  }

  List<Identifiable> getContent();

  void setContent(List<Identifiable> content);

  default void addContent(Identifiable identifiable) {
    if (getContent() == null) {
      setContent(new ArrayList<>());
    }
    getContent().add(identifiable);
  }
}
