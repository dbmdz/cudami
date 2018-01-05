package de.digitalcollections.cudami.model.impl.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.IdentifiableType;
import de.digitalcollections.cudami.model.api.identifiable.resource.Resource;
import de.digitalcollections.cudami.model.api.identifiable.resource.ResourceType;
import de.digitalcollections.cudami.model.impl.identifiable.IdentifiableImpl;

public class ResourceImpl extends IdentifiableImpl implements Resource {
  protected ResourceType resourceType;

  public ResourceImpl() {
    this.type = IdentifiableType.RESOURCE;
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
