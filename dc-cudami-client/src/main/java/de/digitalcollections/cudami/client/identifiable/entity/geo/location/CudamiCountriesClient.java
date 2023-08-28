package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.Country;
import java.net.http.HttpClient;

public class CudamiCountriesClient extends CudamiEntitiesClient<Country> {

  public CudamiCountriesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Country.class, mapper, API_VERSION_PREFIX + "/countries");
  }
}
