package de.digitalcollections.cudami.model.impl.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.resource.parts.ContentBlock;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentBlocksContainer;
import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ContentBlocksContainerImpl extends ResourceImpl implements ContentBlocksContainer {

  private Map<Locale, List<ContentBlock>> contentBlocks;

  public ContentBlocksContainerImpl() {
    super();
    this.resourceType = ResourceType.CONTENTBLOCKS_CONTAINER;
  }

  @Override
  public Map<Locale, List<ContentBlock>> getContentBlocks() {
    return contentBlocks;
  }

  @Override
  public void setContentBlocks(Map<Locale, List<ContentBlock>> contentBlocks) {
    this.contentBlocks = contentBlocks;
  }

}
