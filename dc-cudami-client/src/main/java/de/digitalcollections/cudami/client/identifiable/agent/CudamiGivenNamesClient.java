package de.digitalcollections.cudami.client.identifiable.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.CudamiIdentifiablesClient;
import de.digitalcollections.model.identifiable.agent.GivenName;
import java.net.http.HttpClient;

public class CudamiGivenNamesClient extends CudamiIdentifiablesClient<GivenName> {

  public CudamiGivenNamesClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, GivenName.class, mapper, "/v5/givennames");
  }
}
