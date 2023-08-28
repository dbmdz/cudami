package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.Cave;
import java.net.http.HttpClient;

public class CudamiCavesClient extends CudamiEntitiesClient<Cave> {

  public CudamiCavesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Cave.class, mapper, API_VERSION_PREFIX + "/caves");
  }
}
