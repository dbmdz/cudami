package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiCollectionsClient extends CudamiEntitiesClient<Collection> {

  public CudamiCollectionsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Collection.class, mapper, API_VERSION_PREFIX + "/collections");
  }

  public boolean addDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws TechnicalException {
    try {
      doPostRequestForString(
          String.format(baseEndpoint + "/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid));
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }

  public boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws TechnicalException {
    try {
      doPostRequestForString(
          String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid), digitalObjects);
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }

  public boolean addSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws TechnicalException {
    try {
      doPostRequestForString(
          String.format(baseEndpoint + "/%s/subcollections/%s", collectionUuid, subcollectionUuid));
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }

  public boolean addSubcollections(UUID collectionUuid, List<Collection> subcollections)
      throws TechnicalException {
    try {
      doPostRequestForString(
          String.format(baseEndpoint + "/%s/subcollections", collectionUuid), subcollections);
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }

  public PageResponse<Collection> findActive(PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "?active=true"), pageRequest);
  }

  public PageResponse<Collection> findActiveSubcollections(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections?active=true", uuid),
        pageRequest,
        Collection.class);
  }

  public PageResponse<DigitalObject> findDigitalObjects(
      UUID collectionUuid, PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid),
        pageRequest,
        DigitalObject.class);
  }

  public List<CorporateBody> findRelatedCorporateBodies(UUID uuid, Filtering filtering)
      throws TechnicalException {
    if (filtering.getFilterCriterionFor("predicate") == null) {
      throw new IllegalArgumentException("Filter criterion 'predicate' is required");
    }
    return doGetRequestForObjectList(
        String.format(baseEndpoint + "/%s/related/corporatebodies", uuid),
        CorporateBody.class,
        filtering);
  }

  public PageResponse<Collection> findSubcollections(UUID collectionUuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format(baseEndpoint + "/%s/subcollections", collectionUuid),
        pageRequest,
        Collection.class);
  }

  public PageResponse<Collection> findTopCollections(PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(baseEndpoint + "/top", pageRequest);
  }

  public Collection getActiveByUuid(UUID uuid, Locale locale) throws TechnicalException {
    try {
      return doGetRequestForObject(
          String.format(baseEndpoint + "/%s?active=true&pLocale=%s", uuid, locale));
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws TechnicalException {
    try {
      return (BreadcrumbNavigation)
          doGetRequestForObject(
              String.format(baseEndpoint + "/%s/breadcrumb", uuid), BreadcrumbNavigation.class);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public List<Locale> getLanguagesOfTopCollections() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint + "/top/languages", Locale.class);
  }

  public Collection getParent(UUID uuid) throws TechnicalException {
    return (Collection)
        doGetRequestForObject(String.format(baseEndpoint + "/%s/parent", uuid), Collection.class);
  }

  public List<Collection> getParents(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format(baseEndpoint + "/%s/parents", uuid));
  }

  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws TechnicalException {
    try {
      doDeleteRequestForString(
          String.format(baseEndpoint + "/%s/digitalobjects/%s", collectionUuid, digitalObjectUuid));
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }

  public boolean removeSubcollection(UUID collectionUuid, UUID subcollectionUuid)
      throws TechnicalException {
    try {
      doDeleteRequestForString(
          String.format(baseEndpoint + "/%s/subcollections/%s", collectionUuid, subcollectionUuid));
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }

  public Collection saveWithParentCollection(Collection collection, UUID parentCollectionUuid)
      throws TechnicalException {
    return doPostRequestForObject(
        String.format(baseEndpoint + "/%s/collection", parentCollectionUuid), collection);
  }

  public boolean setDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws TechnicalException {
    try {
      doPutRequestForObject(
          String.format(baseEndpoint + "/%s/digitalobjects", collectionUuid),
          digitalObjects,
          String.class);
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }
}
