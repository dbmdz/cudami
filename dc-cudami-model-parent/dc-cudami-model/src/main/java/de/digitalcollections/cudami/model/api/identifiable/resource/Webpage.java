package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Node;
import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import de.digitalcollections.prosemirror.model.api.Document;

/**
 * A Webpage.
 */
public interface Webpage extends Resource, Node<Webpage> {

  Text getContentBlocks();

  void setContentBlocks(Text contentBlocks);

  Document getContentBlocksContainer();

  void setContentBlocksContainer(Document contentBlocksContainer);
}
