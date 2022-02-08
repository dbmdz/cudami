package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
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

public class CudamiCollectionsClient extends CudamiEntitiesClient<Collection> {

  public CudamiCollectionsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Collection.class, mapper, "/v5/collections");
  }

  public boolean addDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(
                baseEndpoint + "/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid)));
  }

  public boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid), digitalObjects));
  }

  public boolean addSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(
                baseEndpoint + "/%s/subcollections/%s", collectionUuid, subcollectionUuid)));
  }

  public boolean addSubcollections(UUID collectionUuid, List<Collection> subcollections)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doPostRequestForString(
            String.format(baseEndpoint + "/%s/subcollections", collectionUuid), subcollections));
  }

  public PageResponse<Collection> findActive(PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "?active=true"), pageRequest);
  }

  public SearchPageResponse<Collection> findActive(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint + "/search?active", searchPageRequest);
  }

  public Collection findActiveOne(UUID uuid, Locale locale) throws TechnicalException {
    return doGetRequestForObject(
        String.format(baseEndpoint + "/%s?active=true&pLocale=%s", uuid, locale));
  }

  public PageResponse<Collection> findActiveSubcollections(
      UUID uuid, SearchPageRequest searchPageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections?active=true", uuid), searchPageRequest);
  }

  public PageResponse<Collection> findSubcollections(UUID uuid, SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections", uuid), searchPageRequest);
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #findTopCollections(SearchPageRequest)} instead */
  public PageResponse<Collection> findTopCollections(PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(baseEndpoint + "/top", pageRequest);
  }

  public SearchPageResponse<Collection> findTopCollections(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(baseEndpoint + "/top", searchPageRequest);
  }

  public PageResponse<Collection> getActiveSubcollections(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections?active=true", uuid),
        pageRequest,
        Collection.class);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws TechnicalException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format(baseEndpoint + "/%s/breadcrumb", uuid), BreadcrumbNavigation.class);
  }

  @Deprecated(since = "5.0", forRemoval = true)
  /** @deprecated Please use {@link #getDigitalObjects(UUID, SearchPageRequest)} instead */
  public PageResponse<DigitalObject> getDigitalObjects(UUID collectionUuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid),
        pageRequest,
        DigitalObject.class);
  }

  public SearchPageResponse<DigitalObject> getDigitalObjects(
      UUID collectionUuid, SearchPageRequest searchPageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid),
        searchPageRequest,
        DigitalObject.class);
  }

  public Collection getParent(UUID uuid) throws TechnicalException {
    return (Collection)
        doGetRequestForObject(String.format(baseEndpoint + "/%s/parent", uuid), Collection.class);
  }

  public List<Collection> getParents(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format(baseEndpoint + "/%s/parents", uuid));
  }

  public List<CorporateBody> getRelatedCorporateBodies(UUID uuid, Filtering filtering)
      throws TechnicalException {
    if (filtering.getFilterCriterionFor("predicate") == null) {
      throw new IllegalArgumentException("Filter criterion 'predicate' is required");
    }
    return doGetRequestForObjectList(
        String.format(baseEndpoint + "/%s/related/corporatebodies", uuid),
        CorporateBody.class,
        filtering);
  }

  public PageResponse<Collection> getSubcollections(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections", uuid), pageRequest, Collection.class);
  }

  public List<Locale> getTopCollectionsLanguages() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint + "/top/languages", Locale.class);
  }

  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                baseEndpoint + "/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid)));
  }

  public boolean removeSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws TechnicalException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format(
                baseEndpoint + "/%s/subcollections/%s", collectionUuid, subcollectionUuid)));
  }

  public boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws TechnicalException {
    return Boolean.parseBoolean(
        (String)
            doPutRequestForObject(
                String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid),
                digitalObjects,
                String.class));
  }

  public Collection saveWithParentCollection(Collection collection, UUID parentCollectionUuid)
      throws TechnicalException {
    return doPostRequestForObject(
        String.format(baseEndpoint + "/%s/collection", parentCollectionUuid), collection);
  }
}
