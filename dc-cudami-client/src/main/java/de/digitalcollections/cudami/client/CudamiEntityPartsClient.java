package de.digitalcollections.cudami.client;

import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.impl.identifiable.entity.parts.EntityPartImpl;

public class CudamiEntityPartsClient extends CudamiBaseClient<EntityPartImpl> {

  public CudamiEntityPartsClient(String serverUrl) {
    super(serverUrl, EntityPartImpl.class);
  }

  public EntityPart create() {
    return new EntityPartImpl();
  }
}
