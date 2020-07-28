package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.impl.identifiable.entity.parts.EntityPartImpl;
import java.net.http.HttpClient;

public class CudamiEntityPartsClient extends CudamiBaseClient<EntityPartImpl> {

  public CudamiEntityPartsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, EntityPartImpl.class, mapper);
  }

  public EntityPart create() {
    return new EntityPartImpl();
  }
}
