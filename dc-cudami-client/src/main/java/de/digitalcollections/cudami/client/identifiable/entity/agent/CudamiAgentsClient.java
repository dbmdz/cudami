package de.digitalcollections.cudami.client.identifiable.entity.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import java.net.http.HttpClient;

public class CudamiAgentsClient extends CudamiEntitiesClient<Agent> {

  public CudamiAgentsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Agent.class, mapper, API_VERSION_PREFIX + "/agents");
  }
}
