package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.parts.MultilanguageDocument;

/**
 * A Webpage.
 */
public interface Webpage extends Resource, Node<Webpage> {

  MultilanguageDocument getText();

  void setText(MultilanguageDocument multilanguageDocument);
}
