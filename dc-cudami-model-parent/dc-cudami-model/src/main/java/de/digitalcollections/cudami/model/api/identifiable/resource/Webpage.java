package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.parts.Text;

/**
 * A Webpage.
 */
public interface Webpage extends Resource, Node<Webpage> {

  Text getContentBlocks();

  void setContentBlocks(Text contentBlocks);

  ContentBlocksContainer getContentBlocksContainer();

  void setContentBlocksContainer(ContentBlocksContainer contentBlocksContainer);
}
