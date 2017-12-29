package de.digitalcollections.cudami.model.api.identifiable;

public interface Webpage extends Node {

    ContentBlocksContainer getContentBlocksContainer();
    
    void setContentBlocksContainer(ContentBlocksContainer contentBlocksContainer);
}
