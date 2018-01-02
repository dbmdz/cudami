package de.digitalcollections.cudami.model.impl.identifiable;

import de.digitalcollections.cudami.model.api.identifiable.ContentBlocksContainer;
import de.digitalcollections.cudami.model.api.identifiable.Webpage;

public class WebpageImpl extends NodeImpl<Webpage> implements Webpage {

  private ContentBlocksContainer contentBlocksContainer;

  @Override
  public ContentBlocksContainer getContentBlocksContainer() {
    return contentBlocksContainer;
  }

  @Override
  public void setContentBlocksContainer(ContentBlocksContainer contentBlocksContainer) {
    this.contentBlocksContainer = contentBlocksContainer;
  }
  
}
