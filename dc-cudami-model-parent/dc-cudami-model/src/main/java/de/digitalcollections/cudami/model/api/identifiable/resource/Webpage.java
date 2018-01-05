package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Node;

public interface Webpage extends Resource, Node<Webpage> {

    ContentBlocksContainer getContentBlocksContainer();
    
    void setContentBlocksContainer(ContentBlocksContainer contentBlocksContainer);
}
