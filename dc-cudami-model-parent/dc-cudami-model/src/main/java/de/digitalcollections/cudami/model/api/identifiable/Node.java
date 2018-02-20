package de.digitalcollections.cudami.model.api.identifiable;

import java.util.ArrayList;
import java.util.List;

/**
 * Node is used to structure cultural content hierarchically.
 * @param <N> a node
 */
public interface Node<N extends Node> extends Identifiable {

  N getParent();

  void setParent(N parent);

  List<N> getChildren();

  void setChildren(List<N> children);

  default void addChild(N child) {
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
