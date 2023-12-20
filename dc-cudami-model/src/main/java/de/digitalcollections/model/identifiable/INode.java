package de.digitalcollections.model.identifiable;

import de.digitalcollections.model.text.LocalizedText;
import java.util.ArrayList;
import java.util.List;

public interface INode<N extends Identifiable> {

  default void addChild(N child) {
    if (getChildren() == null) {
      setChildren(new ArrayList<>(0));
    }
    getChildren().add(child);
  }

  List<N> getChildren();

  LocalizedText getLabel();

  N getParent();

  void setChildren(List<N> children);

  void setParent(N parent);
}
