package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
import java.net.http.HttpClient;

public class CudamiPublishersClient extends CudamiRestClient<Publisher> {

  public CudamiPublishersClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Publisher.class, mapper, API_VERSION_PREFIX + "/publishers");
  }
}
