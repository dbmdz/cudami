package de.digitalcollections.cudami.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
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
        doPostRequestForString(
            String.format(
                "/latest/collections/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid)));
  }

  public boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("/latest/collections/%s/digitalobjects", collectionUuid),
            digitalObjects));
  }

  public boolean addSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(
                "/latest/collections/%s/subcollections/%s", collectionUuid, subcollectionUuid)));
  }

  public boolean addSubcollections(UUID collectionUuid, List<Collection> subcollections)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("/latest/collections/%s/subcollections", collectionUuid),
            subcollections));
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/latest/collections/count"));
  }

  public Collection create() {
    return new CollectionImpl();
  }

  public PageResponse<CollectionImpl> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/collections", pageRequest);
  }

  public SearchPageResponse<CollectionImpl> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/collections/search", searchPageRequest);
  }

  public List<CollectionImpl> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest =
        new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<CollectionImpl> response = find(searchPageRequest);
    return response.getContent();
  }

  public PageResponse<CollectionImpl> findActive(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/collections?active=true"), pageRequest);
  }

  public SearchPageResponse<CollectionImpl> findActive(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        "/latest/collections/search?active", searchPageRequest);
  }

  public Collection findActiveOne(UUID uuid, Locale locale) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/collections/%s?active=true&pLocale=%s", uuid, locale));
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

  public PageResponse<Collection> getActiveSubcollections(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/collections/%s/subcollections?active=true", uuid),
        pageRequest,
        CollectionImpl.class);
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

  public Collection getParent(UUID uuid) throws HttpException {
    return (Collection)
        doGetRequestForObject(
            String.format("/latest/collections/%s/parent", uuid), CollectionImpl.class);
  }

  public List<CollectionImpl> getParents(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/collections/%s/parents", uuid));
  }

  public PageResponse<Collection> getSubcollections(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/collections/%s/subcollections", uuid),
        pageRequest,
        CollectionImpl.class);
  }

  public List<CorporateBodyImpl> getRelatedCorporateBodies(UUID uuid, Filtering filtering)
      throws HttpException {
    if (filtering.getFilterCriterionFor("predicate") == null) {
      throw new IllegalArgumentException("Filter criterion 'predicate' is required");
    }
    return doGetRequestForObjectList(
        String.format("/latest/collections/%s/related/corporatebodies", uuid),
        CorporateBodyImpl.class,
        filtering);
  }

  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                "/latest/collections/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid)));
  }

  public boolean removeSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                "/latest/collections/%s/subcollections/%s", collectionUuid, subcollectionUuid)));
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

  public Collection saveWithParentCollection(Collection collection, UUID parentCollectionUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/collections/%s/collection", parentCollectionUuid),
        (CollectionImpl) collection);
  }

  public Collection update(UUID uuid, Collection collection) throws HttpException {
    return doPutRequestForObject(
        String.format("/latest/collections/%s", uuid), (CollectionImpl) collection);
  }
}
