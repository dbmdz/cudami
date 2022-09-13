package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.identifiable.entity.work.Publication;
import java.net.http.HttpClient;

public class CudamiPublicationsClient extends CudamiRestClient<Publication> {

  public CudamiPublicationsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Publication.class, mapper, API_VERSION_PREFIX + "/publications");
  }
}
