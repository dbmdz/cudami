package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.River;
import java.net.http.HttpClient;

public class CudamiRiversClient extends CudamiEntitiesClient<River> {

  public CudamiRiversClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, River.class, mapper, API_VERSION_PREFIX + "/rivers");
  }
}
