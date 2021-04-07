package de.digitalcollections.cudami.client.entity.work;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.ItemImpl;
import de.digitalcollections.model.impl.identifiable.entity.work.WorkImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.UUID;

public class CudamiItemsClient extends CudamiBaseClient<ItemImpl> {

  public CudamiItemsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, ItemImpl.class, mapper);
  }

  public Boolean addDigitalObject(UUID itemUuid, UUID digitalObjectUuid) throws HttpException {
    return (Boolean)
        doPostRequestForObject(
            String.format("/v2/items/%s/digitalobjects/%s", itemUuid, digitalObjectUuid),
            Boolean.class);
  }

  public boolean addWork(UUID itemUuid, UUID workUuid) throws HttpException {
    return (boolean)
        doPostRequestForObject(
            String.format("/v2/items/%s/works/%s", itemUuid, workUuid), Boolean.class);
  }

  public Item create() {
    return new ItemImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v2/items/count"));
  }

  public PageResponse<ItemImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v2/items", pageRequest);
  }

  public PageResponse findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws HttpException {
    return findByLanguageAndInitial("/v2/items", pageRequest, language, initial);
  }

  public PageResponse<ItemImpl> findByLanguageAndInitial(
      int pageNumber,
      int pageSize,
      String sortField,
      String sortDirection,
      String nullHandling,
      String language,
      String initial)
      throws HttpException {
    return findByLanguageAndInitial(
        "/v2/items",
        pageNumber,
        pageSize,
        sortField,
        sortDirection,
        nullHandling,
        language,
        initial);
  }

  public Item findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v2/items/%s", uuid));
  }

  public Item findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v2/items/identifier?namespace=%s&id=%s", namespace, id));
  }

  public List getDigitalObjects(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v2/items/%s/digitalobjects", uuid), DigitalObjectImpl.class);
  }

  public List getWorks(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v2/items/%s/works", uuid), WorkImpl.class);
  }

  public Item save(Item item) throws HttpException {
    return doPostRequestForObject("/v2/items", (ItemImpl) item);
  }

  public Item update(UUID uuid, Item item) throws HttpException {
    return doPutRequestForObject(String.format("/v2/items/%s", uuid), (ItemImpl) item);
  }
}
