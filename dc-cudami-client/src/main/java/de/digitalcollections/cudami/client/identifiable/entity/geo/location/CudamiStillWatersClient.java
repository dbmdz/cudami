package de.digitalcollections.cudami.client.identifiable.entity.geo.location;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.geo.location.StillWaters;
import java.net.http.HttpClient;

public class CudamiStillWatersClient extends CudamiEntitiesClient<StillWaters> {

  public CudamiStillWatersClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, StillWaters.class, mapper, API_VERSION_PREFIX + "/stillwaters");
  }
}
