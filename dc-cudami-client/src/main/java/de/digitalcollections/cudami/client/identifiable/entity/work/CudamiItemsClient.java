package de.digitalcollections.cudami.client.identifiable.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiItemsClient extends CudamiBaseClient<Item> {

  public CudamiItemsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Item.class, mapper);
  }

  public Boolean addDigitalObject(UUID itemUuid, UUID digitalObjectUuid) throws HttpException {
    return (Boolean)
        doPostRequestForObject(
            String.format("/v5/items/%s/digitalobjects/%s", itemUuid, digitalObjectUuid),
            Boolean.class);
  }

  public boolean addWork(UUID itemUuid, UUID workUuid) throws HttpException {
    return (boolean)
        doPostRequestForObject(
            String.format("/v5/items/%s/works/%s", itemUuid, workUuid), Boolean.class);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/items/count"));
  }

  public Item create() {
    return new Item();
  }

  public PageResponse<Item> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/items", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/v5/items", pageRequest, language, initial);
  }

  public PageResponse<Item> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/v5/items",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public Item findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/items/%s", uuid));
  }

  public Item findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/items/identifier?namespace=%s&id=%s", namespace, id));
  }

  public List getDigitalObjects(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/items/%s/digitalobjects", uuid), DigitalObject.class);
  }

  public List getWorks(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v5/items/%s/works", uuid), Work.class);
  }

  public Item save(Item item) throws HttpException {
    return doPostRequestForObject("/v5/items", item);
  }

  public Item update(UUID uuid, Item item) throws HttpException {
    return doPutRequestForObject(String.format("/v5/items/%s", uuid), item);
  }
}
