package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CudamiWorksClient extends CudamiEntitiesClient<Work> {

  public CudamiWorksClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Work.class, mapper, API_VERSION_PREFIX + "/works");
  }

  public Set<Agent> getCreators(UUID uuid) throws TechnicalException {
    return (Set<Agent>)
        doGetRequestForObjectList(
            String.format("%s/%s/creators", baseEndpoint, uuid), DigitalObject.class);
  }

  public List getItems(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/%s/items", baseEndpoint, uuid), Item.class);
  }

  public PageResponse<Work> findChildren(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/" + uuid + "/children"), pageRequest);
  }
}
