package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.Creek;
import java.net.http.HttpClient;

public class CudamiCreeksClient extends CudamiEntitiesClient<Creek> {

  public CudamiCreeksClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Creek.class, mapper, API_VERSION_PREFIX + "/creeks");
  }
}
