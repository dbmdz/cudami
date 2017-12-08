package de.digitalcollections.cudami.model.api.entity;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import java.util.ArrayList;
import java.util.List;

/**
 * Article is used to manage cultural articles and their hierarchy.
 */
public interface Article extends Identifiable {

  Article getParent();

  void setParent(Article parent);

  List<Article> getChildren();

  void setChildren(List<Article> children);

  default void addChild(Article child) {
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
