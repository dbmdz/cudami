package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.Continent;
import java.net.http.HttpClient;

public class CudamiContinentsClient extends CudamiEntitiesClient<Continent> {

  public CudamiContinentsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Continent.class, mapper, API_VERSION_PREFIX + "/continents");
  }
}
