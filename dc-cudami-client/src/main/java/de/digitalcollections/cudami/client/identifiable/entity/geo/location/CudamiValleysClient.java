package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.Valley;
import java.net.http.HttpClient;

public class CudamiValleysClient extends CudamiEntitiesClient<Valley> {

  public CudamiValleysClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Valley.class, mapper, API_VERSION_PREFIX + "/valleys");
  }
}
