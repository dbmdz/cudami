package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiCollectionsClient extends CudamiBaseClient<Collection> {

  public CudamiCollectionsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Collection.class, mapper);
  }

  public boolean addDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(
                "/v5/collections/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid)));
  }

  public boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("/v5/collections/%s/digitalobjects", collectionUuid), digitalObjects));
  }

  public boolean addSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(
                "/v5/collections/%s/subcollections/%s", collectionUuid, subcollectionUuid)));
  }

  public boolean addSubcollections(UUID collectionUuid, List<Collection> subcollections)
      throws HttpException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format("/v5/collections/%s/subcollections", collectionUuid), subcollections));
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/collections/count"));
  }

  public Collection create() {
    return new Collection();
  }

  public PageResponse<Collection> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/collections", pageRequest);
  }

  public SearchPageResponse<Collection> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v5/collections/search", searchPageRequest);
  }

  public List<Collection> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<Collection> response = find(searchPageRequest);
    return response.getContent();
  }

  public PageResponse<Collection> findActive(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/collections?active=true"), pageRequest);
  }

  public SearchPageResponse<Collection> findActive(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v5/collections/search?active", searchPageRequest);
  }

  public Collection findActiveOne(UUID uuid, Locale locale) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/collections/%s?active=true&pLocale=%s", uuid, locale));
  }

  public PageResponse<Collection> findActiveSubcollections(
      UUID uuid, SearchPageRequest searchPageRequest) throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/v5/collections/%s/subcollections?active=true", uuid), searchPageRequest);
  }

  public Collection findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/collections/%s", uuid));
  }

  public Collection findOne(UUID uuid, Locale locale) throws HttpException {
    return findOne(uuid, locale.toString());
  }

  public Collection findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v5/collections/%s?pLocale=%s", uuid, locale));
  }

  public Collection findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/v5/collections/identifier/%s:%s.json", namespace, id));
  }

  public Collection findOneByRefId(long refId) throws HttpException {
    return doGetRequestForObject(String.format("/v5/collections/%d", refId));
  }

  public PageResponse<Collection> findSubcollections(UUID uuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/v5/collections/%s/subcollections", uuid), searchPageRequest);
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #findTopCollections(SearchPageRequest)} instead */
  public PageResponse<Collection> findTopCollections(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/collections/top", pageRequest);
  }

  public SearchPageResponse<Collection> findTopCollections(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v5/collections/top", searchPageRequest);
  }

  public PageResponse<Collection> getActiveSubcollections(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/collections/%s/subcollections?active=true", uuid),
        pageRequest,
        Collection.class);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/v5/collections/%s/breadcrumb", uuid), BreadcrumbNavigation.class);
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #getDigitalObjects(UUID, SearchPageRequest)} instead */
  public PageResponse<DigitalObject> getDigitalObjects(UUID collectionUuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/collections/%s/digitalobjects", collectionUuid),
        pageRequest,
        DigitalObject.class);
  }

  public SearchPageResponse<DigitalObject> getDigitalObjects(
      UUID collectionUuid, SearchPageRequest searchPageRequest) throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/v5/collections/%s/digitalobjects", collectionUuid),
        searchPageRequest,
        DigitalObject.class);
  }

  public Collection getParent(UUID uuid) throws HttpException {
    return (Collection)
        doGetRequestForObject(String.format("/v5/collections/%s/parent", uuid), Collection.class);
  }

  public List<Collection> getParents(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v5/collections/%s/parents", uuid));
  }

  public PageResponse<Collection> getSubcollections(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/collections/%s/subcollections", uuid), pageRequest, Collection.class);
  }

  public List<CorporateBody> getRelatedCorporateBodies(UUID uuid, Filtering filtering)
      throws HttpException {
    if (filtering.getFilterCriterionFor("predicate") == null) {
      throw new IllegalArgumentException("Filter criterion 'predicate' is required");
    }
    return doGetRequestForObjectList(
        String.format("/v5/collections/%s/related/corporatebodies", uuid),
        CorporateBody.class,
        filtering);
  }

  public List<Locale> getTopCollectionsLanguages() throws HttpException {
    return doGetRequestForObjectList("/v5/collections/top/languages", Locale.class);
  }

  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                "/v5/collections/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid)));
  }

  public boolean removeSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                "/v5/collections/%s/subcollections/%s", collectionUuid, subcollectionUuid)));
  }

  public Collection save(Collection collection) throws HttpException {
    return doPostRequestForObject("/v5/collections", collection);
  }

  public boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws HttpException {
    return Boolean.parseBoolean(
        (String)
            doPutRequestForObject(
                String.format("/v5/collections/%s/digitalobjects", collectionUuid),
                digitalObjects,
                String.class));
  }

  public Collection saveWithParentCollection(Collection collection, UUID parentCollectionUuid)
      throws HttpException {
    return doPostRequestForObject(
        String.format("/v5/collections/%s/collection", parentCollectionUuid), collection);
  }

  public Collection update(UUID uuid, Collection collection) throws HttpException {
    return doPutRequestForObject(String.format("/v5/collections/%s", uuid), collection);
  }
}
