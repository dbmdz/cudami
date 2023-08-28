package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.Mountain;
import java.net.http.HttpClient;

public class CudamiMountainsClient extends CudamiEntitiesClient<Mountain> {

  public CudamiMountainsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Mountain.class, mapper, API_VERSION_PREFIX + "/mountains");
  }
}
