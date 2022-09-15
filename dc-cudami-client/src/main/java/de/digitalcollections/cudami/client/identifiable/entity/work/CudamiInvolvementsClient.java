package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiRestClient;
import de.digitalcollections.model.identifiable.entity.work.Involvement;
import java.net.http.HttpClient;

public class CudamiInvolvementsClient extends CudamiRestClient<Involvement> {

  public CudamiInvolvementsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Involvement.class, mapper, API_VERSION_PREFIX + "/involvements");
  }
}
