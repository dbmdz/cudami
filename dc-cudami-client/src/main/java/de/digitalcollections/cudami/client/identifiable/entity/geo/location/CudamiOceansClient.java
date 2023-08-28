package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.Ocean;
import java.net.http.HttpClient;

public class CudamiOceansClient extends CudamiEntitiesClient<Ocean> {

  public CudamiOceansClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Ocean.class, mapper, API_VERSION_PREFIX + "/oceans");
  }
}
