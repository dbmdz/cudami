package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.Canyon;
import java.net.http.HttpClient;

public class CudamiCanyonsClient extends CudamiEntitiesClient<Canyon> {

  public CudamiCanyonsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Canyon.class, mapper, API_VERSION_PREFIX + "/canyons");
  }
}
