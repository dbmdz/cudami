package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.identifiable.entity.CudamiEntitiesClient;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiItemsClient extends CudamiEntitiesClient<Item> {

  public CudamiItemsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Item.class, mapper, API_VERSION_PREFIX + "/items");
  }

  public Boolean addDigitalObject(UUID itemUuid, UUID digitalObjectUuid) throws TechnicalException {
    return (Boolean)
        doPostRequestForObject(
            String.format("%s/%s/digitalobjects/%s", baseEndpoint, itemUuid, digitalObjectUuid),
            Boolean.class);
  }

  public boolean addWork(UUID itemUuid, UUID workUuid) throws TechnicalException {
    return (boolean)
        doPostRequestForObject(
            String.format("%s/%s/works/%s", baseEndpoint, itemUuid, workUuid), Boolean.class);
  }

  public PageResponse<DigitalObject> findDigitalObjects(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/digitalobjects", baseEndpoint, uuid),
        pageRequest,
        DigitalObject.class);
  }

  public PageResponse<Item> getAllForParent(Item parent) throws TechnicalException {
    if (parent == null) {
      throw new TechnicalException("Empty parent");
    }

    PageRequest pageRequest = PageRequest.builder().pageNumber(0).pageSize(10000).build();
    return getAllForParent(parent, pageRequest);
  }

  public PageResponse<Item> getAllForParent(Item parent, PageRequest pageRequest)
      throws TechnicalException {
    pageRequest.add(
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("part_of_item.uuid")
                    .isEquals(parent.getUuid())
                    .build())
            .build());
    return find(pageRequest);
  }

  public List<Locale> getLanguagesOfDigitalObjects(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/digitalobjects/languages", baseEndpoint, uuid), Locale.class);
  }

  public List getWorks(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/%s/works", baseEndpoint, uuid), Work.class);
  }
}
