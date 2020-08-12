package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiCollectionsClient extends CudamiBaseClient<CollectionImpl> {

  public CudamiCollectionsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, CollectionImpl.class, mapper);
  }

  public boolean addDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doPatchRequestForString(
            String.format(
                "/latest/collections/%s/digitalobject/%s", collectionUuid, digitalObjectUuid)));
  }

  public boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        doPatchRequestForString(
            String.format("/latest/collections/%s/digitalobjects", collectionUuid),
            digitalObjects));
  }

  public Collection create() {
    return new CollectionImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/collections/count"));
  }

  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                "/latest/collections/%s/digitalobject/%s", collectionUuid, digitalObjectUuid)));
  }

  public PageResponse<CollectionImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/collections", pageRequest);
  }

  public Collection findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/collections/%s", uuid));
  }

  public Collection findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Collection findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/collections/%s?pLocale=%s", uuid, locale));
  }

  public Collection findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/collections/identifier/%s:%s.json", namespace, id));
  }

  public PageResponse<CollectionImpl> findTopCollections(PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList("/latest/collections/top", pageRequest);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/latest/collections/%s/breadcrumb", uuid),
            BreadcrumbNavigationImpl.class);
  }

  public PageResponse<DigitalObject> getDigitalObjects(UUID collectionUuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/collections/%s/digitalobjects", collectionUuid),
        pageRequest,
        DigitalObjectImpl.class);
  }

  public Collection saveWithParentCollection(Collection collection, UUID parentCollectionUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/collections/%s/collection", parentCollectionUuid),
        (CollectionImpl) collection);
  }

  public Collection save(Collection collection) throws HttpException {
    return doPostRequestForObject("/latest/collections", (CollectionImpl) collection);
  }

  public boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        (String)
            doPutRequestForObject(
                String.format("/latest/collections/%s/digitalobjects", collectionUuid),
                digitalObjects,
                String.class));
  }

  public Collection update(UUID uuid, Collection collection) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/collections/%s", uuid), (CollectionImpl) collection);
  }
}
