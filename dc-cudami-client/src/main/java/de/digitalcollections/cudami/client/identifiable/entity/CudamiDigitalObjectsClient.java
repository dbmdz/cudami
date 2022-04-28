package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.filter.Filtering;
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

  public SearchPageResponse<Collection> findActiveCollections(
      UUID uuid, SearchPageRequest searchPageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/collections?active=true", baseEndpoint, uuid),
        searchPageRequest,
        Collection.class);
  }

  public SearchPageResponse<Collection> findCollections(
      UUID uuid, SearchPageRequest searchPageRequest) throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/collections", baseEndpoint, uuid),
        searchPageRequest,
        Collection.class);
  }

  public SearchPageResponse<Project> findProjects(UUID uuid, SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/projects", baseEndpoint, uuid), searchPageRequest, Project.class);
  }

  public PageResponse<DigitalObject> getAllForParent(DigitalObject parent)
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

  public List<DigitalObject> getAllReduced() throws TechnicalException {
    return doGetRequestForObjectList(baseEndpoint + "/reduced", DigitalObject.class);
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

  public List<DigitalObject> getRandomDigitalObjects(int count) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/random?count=%d", baseEndpoint, count), DigitalObject.class);
  }

  public List<FileResource> setFileResources(UUID uuid, List fileResources)
      throws TechnicalException {
    return doPostRequestForObjectList(
        String.format("%s/%s/fileresources", baseEndpoint, uuid),
        fileResources,
        FileResource.class);
  }
}
