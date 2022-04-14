package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CudamiWorksClient extends CudamiEntitiesClient<Work> {

  public CudamiWorksClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Work.class, mapper, "/v5/works");
  }

  public Set<Agent> findCreators(UUID uuid) throws TechnicalException {
    return (Set<Agent>)
        doGetRequestForObjectList(
            String.format("%s/%s/creators", baseEndpoint, uuid), DigitalObject.class);
  }

  public List findItems(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/%s/items", baseEndpoint, uuid), Item.class);
  }
}
