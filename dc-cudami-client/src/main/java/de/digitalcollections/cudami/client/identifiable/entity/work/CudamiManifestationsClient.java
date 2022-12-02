package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import java.net.http.HttpClient;

public class CudamiManifestationsClient extends CudamiEntitiesClient<Manifestation> {

  public CudamiManifestationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Manifestation.class, mapper, API_VERSION_PREFIX + "/manifestations");
  }
}
