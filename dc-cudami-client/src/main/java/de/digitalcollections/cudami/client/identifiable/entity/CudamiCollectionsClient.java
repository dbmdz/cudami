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
    return new Collection();
  }

  public PageResponse<Collection> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/collections", pageRequest);
  }

  public SearchPageResponse<Collection> find(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/collections/search", searchPageRequest);
  }

  public List<Collection> find(String searchTerm, int maxResults) throws HttpException {
    SearchPageRequest searchPageRequest = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<Collection> response = find(searchPageRequest);
    return response.getContent();
  }

  public PageResponse<Collection> findActive(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/collections?active=true"), pageRequest);
  }

  public SearchPageResponse<Collection> findActive(SearchPageRequest searchPageRequest)
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

  public Collection findOneByRefId(long refId) throws HttpException {
    return doGetRequestForObject(String.format("/latest/collections/%d", refId));
  }

  public PageResponse<Collection> findTopCollections(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/collections/top", pageRequest);
  }

  public PageResponse<Collection> findTopCollections(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/latest/collections/top", searchPageRequest);
  }

  public PageResponse<Collection> getActiveSubcollections(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/collections/%s/subcollections?active=true", uuid),
        pageRequest,
        Collection.class);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/latest/collections/%s/breadcrumb", uuid), BreadcrumbNavigation.class);
  }

  public SearchPageResponse<DigitalObject> getDigitalObjects(UUID collectionUuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/latest/collections/%s/digitalobjects", collectionUuid),
        searchPageRequest,
        DigitalObject.class);
  }

  public Collection getParent(UUID uuid) throws HttpException {
    return (Collection)
        doGetRequestForObject(
            String.format("/latest/collections/%s/parent", uuid), Collection.class);
  }

  public List<Collection> getParents(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/collections/%s/parents", uuid));
  }

  public PageResponse<Collection> getSubcollections(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/collections/%s/subcollections", uuid),
        pageRequest,
        Collection.class);
  }

  public List<CorporateBody> getRelatedCorporateBodies(UUID uuid, Filtering filtering)
      throws HttpException {
    if (filtering.getFilterCriterionFor("predicate") == null) {
      throw new IllegalArgumentException("Filter criterion 'predicate' is required");
    }
    return doGetRequestForObjectList(
        String.format("/latest/collections/%s/related/corporatebodies", uuid),
        CorporateBody.class,
        filtering);
  }

  public List<Locale> getTopCollectionsLanguages() throws HttpException {
    return doGetRequestForObjectList("/latest/collections/top/languages", Locale.class);
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
    return doPostRequestForObject("/latest/collections", collection);
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
        String.format("/latest/collections/%s/collection", parentCollectionUuid), collection);
  }

  public Collection update(UUID uuid, Collection collection) throws HttpException {
    return doPutRequestForObject(String.format("/latest/collections/%s", uuid), collection);
  }
}
