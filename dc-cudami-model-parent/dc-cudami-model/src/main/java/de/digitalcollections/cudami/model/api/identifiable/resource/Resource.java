package de.digitalcollections.cudami.model.api.identifiable.resource;

import de.digitalcollections.cudami.model.api.identifiable.Identifiable;

public interface Resource extends Identifiable {

  ResourceType getResourceType();

  void setResourceType(ResourceType resourceType);
}
