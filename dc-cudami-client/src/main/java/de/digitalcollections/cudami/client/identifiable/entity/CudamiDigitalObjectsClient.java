package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
<<<<<<< HEAD
import de.digitalcollections.model.filter.Filtering;
=======
import de.digitalcollections.model.filter.FilterCriterion;
import de.digitalcollections.model.filter.FilterOperation;
import de.digitalcollections.model.filter.FilteringBuilder;
>>>>>>> Version dependency downgrade
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageRequestBuilder;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiDigitalObjectsClient extends CudamiEntitiesClient<DigitalObject> {

  public CudamiDigitalObjectsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, DigitalObject.class, mapper, "/v5/digitalobjects");
  }

  public List<DigitalObject> findAllReduced() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint + "/reduced", DigitalObject.class);
  }

  public PageResponse<DigitalObject> findAllForParent(DigitalObject parent)
      throws TechnicalException {
    if (parent == null) {
      throw new TechnicalException("Empty parent");
    }

    PageRequest pageRequest =
        new PageRequestBuilder()
            .pageNumber(0)
            .pageSize(10000)
            .filtering(
                Filtering.defaultBuilder().filter("parent.uuid").isEquals(parent.getUuid()).build())
            .build();
    return find(pageRequest);
  }

  public PageResponse<DigitalObject> findRandomDigitalObjects(int count) throws TechnicalException {
    PageRequest pageRequest = new PageRequest(0, count, null);
    return doGetRequestForPagedObjectList(baseEndpoint + "/random", pageRequest);
  }

  public SearchPageResponse<Collection> getActiveCollections(
      UUID uuid, SearchPageRequest searchPageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/collections?active=true", baseEndpoint, uuid),
        searchPageRequest,
        Collection.class);
  }

  public SearchPageResponse<Collection> getCollections(
      UUID uuid, SearchPageRequest searchPageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/collections", baseEndpoint, uuid),
        searchPageRequest,
        Collection.class);
  }

  public List<FileResource> getFileResources(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/fileresources", baseEndpoint, uuid), FileResource.class);
  }

  public List<ImageFileResource> getImageFileResources(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/fileresources/images", baseEndpoint, uuid), ImageFileResource.class);
  }

  public Item getItem(UUID uuid) throws TechnicalException {
    return (Item)
        doGetRequestForObject(String.format("%s/%s/item", baseEndpoint, uuid), Item.class);
  }

  public List<Locale> getLanguages() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint + "/languages", Locale.class);
  }

  public List<Locale> getLanguagesOfCollections(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/collections/languages", baseEndpoint, uuid), Locale.class);
  }

  public List<Locale> getLanguagesOfProjects(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/projects/languages", baseEndpoint, uuid), Locale.class);
  }

  public SearchPageResponse<Project> getProjects(UUID uuid, SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/projects", baseEndpoint, uuid), searchPageRequest, Project.class);
  }

  public List<FileResource> saveFileResources(UUID uuid, List fileResources)
      throws TechnicalException {
    return doPostRequestForObjectList(
        String.format("%s/%s/fileresources", baseEndpoint, uuid),
        fileResources,
        FileResource.class);
  }
}
