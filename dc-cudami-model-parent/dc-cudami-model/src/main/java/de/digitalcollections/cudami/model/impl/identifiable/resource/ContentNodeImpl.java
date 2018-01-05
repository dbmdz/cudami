package de.digitalcollections.cudami.model.impl.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import de.digitalcollections.cudami.model.impl.identifiable.NodeImpl;

public class ContentNodeImpl extends NodeImpl<ContentNode> implements ContentNode {

  private ResourceType resourceType;

  public ContentNodeImpl() {
    super();
    this.resourceType = ResourceType.CONTENTNODE;
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
