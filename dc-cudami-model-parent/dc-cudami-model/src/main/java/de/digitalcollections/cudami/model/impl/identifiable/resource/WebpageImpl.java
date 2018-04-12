package de.digitalcollections.cudami.model.impl.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentBlocksContainer;
import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.model.impl.identifiable.NodeImpl;

public class WebpageImpl extends NodeImpl<Webpage> implements Webpage {

  private Text contentBlocks;

  private ContentBlocksContainer contentBlocksContainer;
  private ResourceType resourceType;

  public WebpageImpl() {
    super();
    this.resourceType = ResourceType.WEBPAGE;
  }

  @Override
  public Text getContentBlocks() {
    return contentBlocks;
  }

  @Override
  public void setContentBlocks(Text contentBlocks) {
    this.contentBlocks = contentBlocks;
  }

  @Override
  public ContentBlocksContainer getContentBlocksContainer() {
    return contentBlocksContainer;
  }

  @Override
  public void setContentBlocksContainer(ContentBlocksContainer contentBlocksContainer) {
    this.contentBlocksContainer = contentBlocksContainer;
  }

  @Override
  public ResourceType getResourceType() {
    return resourceType;
  }

  @Override
  public void setResourceType(ResourceType resourceType) {
    this.resourceType = resourceType;
  }
}
