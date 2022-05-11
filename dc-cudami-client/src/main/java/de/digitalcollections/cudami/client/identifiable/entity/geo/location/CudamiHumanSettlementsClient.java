package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import java.net.http.HttpClient;

public class CudamiHumanSettlementsClient extends CudamiEntitiesClient<HumanSettlement> {

  public CudamiHumanSettlementsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, HumanSettlement.class, mapper, API_VERSION_PREFIX + "/humansettlements");
  }
}
