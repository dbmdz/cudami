package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.Lake;
import java.net.http.HttpClient;

public class CudamiLakesClient extends CudamiEntitiesClient<Lake> {

  public CudamiLakesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Lake.class, mapper, API_VERSION_PREFIX + "/lakes");
  }
}
