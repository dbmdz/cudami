package de.digitalcollections.cudami.client;

import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
import java.util.Locale;
import java.util.UUID;

public class CudamiCollectionsClient extends CudamiBaseClient<CollectionImpl> {

  public CudamiCollectionsClient(String serverUrl) {
    super(serverUrl, CollectionImpl.class);
  }

  public Collection create() {
    return new CollectionImpl();
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/collections/count"));
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

  public Collection saveWithParentCollection(Collection collection, UUID parentCollectionUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/collections/%s/collection", parentCollectionUuid),
        (CollectionImpl) collection);
  }

  public Collection save(Collection collection) throws HttpException {
    return doPostRequestForObject("/latest/collections", (CollectionImpl) collection);
  }

  public Collection update(UUID uuid, Collection collection) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/collections/%s", uuid), (CollectionImpl) collection);
  }
}
