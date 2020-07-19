package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.impl.identifiable.entity.parts.EntityPartImpl;

public class CudamiEntityPartsClient extends CudamiBaseClient<EntityPartImpl> {

  public CudamiEntityPartsClient(String serverUrl, ObjectMapper mapper) {
    super(serverUrl, EntityPartImpl.class, mapper);
  }

  public EntityPart create() {
    return new EntityPartImpl();
  }
}
