package de.digitalcollections.cudami.model.api.identifiable;

public interface Webpage extends Node<Webpage> {

    ContentBlocksContainer getContentBlocksContainer();
    
    void setContentBlocksContainer(ContentBlocksContainer contentBlocksContainer);
}
