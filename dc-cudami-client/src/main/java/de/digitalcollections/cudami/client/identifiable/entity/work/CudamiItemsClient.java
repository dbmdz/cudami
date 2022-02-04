package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.http.HttpException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiItemsClient extends CudamiEntitiesClient<Item> {

  public CudamiItemsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Item.class, mapper, "/v5/items");
  }

  public Boolean addDigitalObject(UUID itemUuid, UUID digitalObjectUuid) throws HttpException {
    return (Boolean)
        doPostRequestForObject(
            String.format("%s/%s/digitalobjects/%s", baseEndpoint, itemUuid, digitalObjectUuid),
            Boolean.class);
  }

  public boolean addWork(UUID itemUuid, UUID workUuid) throws HttpException {
    return (boolean)
        doPostRequestForObject(
            String.format("%s/%s/works/%s", baseEndpoint, itemUuid, workUuid), Boolean.class);
  }

  public List getDigitalObjects(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("%s/%s/digitalobjects", baseEndpoint, uuid), DigitalObject.class);
  }

  public List getWorks(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("%s/%s/works", baseEndpoint, uuid), Work.class);
  }
}
