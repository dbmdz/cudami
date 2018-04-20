package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Node;

/**
 * A Webpage.
 */
public interface Webpage extends Resource, Node<Webpage> {

  MultilanguageDocument getMultilanguageDocument();

  void setMultilanguageDocument(MultilanguageDocument multilanguageDocument);
}
