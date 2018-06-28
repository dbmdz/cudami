package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;
import java.util.List;

/**
 * A Webpage.
 */
public interface Webpage extends Resource, Node<Webpage> {

  List<Webpage> getSubPages();

  void setSubPages(List<Webpage> subPages);

  MultilanguageDocument getText();

  void setText(MultilanguageDocument multilanguageDocument);
}
