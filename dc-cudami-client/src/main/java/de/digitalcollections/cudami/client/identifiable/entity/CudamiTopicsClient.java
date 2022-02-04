package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.http.HttpException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiTopicsClient extends CudamiEntitiesClient<Topic> {

  public CudamiTopicsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Topic.class, mapper, "/v5/topics");
  }

  @Override
  public SearchPageResponse<Topic> find(SearchPageRequest pageRequest) throws HttpException {
    // FIXME /search or not. make everywhere the same endpoint syntax please!
    return this.doGetSearchRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public PageResponse<Topic> findSubtopics(UUID uuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/subtopics", baseEndpoint, uuid), searchPageRequest);
  }

  /**
   * @deprecated This method is subject to be removed.
   *     <p>Use {@link
   *     CudamiTopicsClient#findTopTopics(de.digitalcollections.model.paging.PageRequest)} instead.
   * @param searchPageRequest search params
   * @return object with given refId
   */
  @Deprecated(forRemoval = true)
  public SearchPageResponse<Topic> findTopCollections(SearchPageRequest searchPageRequest)
      throws HttpException {
    // FIXME: findTopCollections? wrong method, we just have top topics!
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/top", baseEndpoint), searchPageRequest);
  }

  public PageResponse<Topic> findTopTopics(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(String.format("%s/top", baseEndpoint), pageRequest);
  }

  public List<Entity> getAllEntities(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("%s/%s/entities/all", baseEndpoint, uuid), Entity.class);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("%s/%s/breadcrumb", baseEndpoint, uuid), BreadcrumbNavigation.class);
  }

  public List<Topic> getChildren(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("%s/%s/children", baseEndpoint, uuid));
  }

  public PageResponse<Topic> getChildren(UUID uuid, PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children", baseEndpoint, uuid), pageRequest);
  }

  public PageResponse<Entity> getEntities(UUID uuid, PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/entities", baseEndpoint, uuid), pageRequest, Entity.class);
  }

  public PageResponse<FileResource> getFileResources(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/fileresources", baseEndpoint, uuid), pageRequest, FileResource.class);
  }

  public List<Locale> getLanguagesOfEntities(UUID topicUuid) throws HttpException {
    return this.doGetRequestForObjectList(
        String.format("%s/%s/entities/languages", baseEndpoint, topicUuid), Locale.class);
  }

  public List<Locale> getLanguagesOfFileResources(UUID topicUuid) throws HttpException {
    return this.doGetRequestForObjectList(
        String.format("%s/%s/fileresources/languages", baseEndpoint, topicUuid), Locale.class);
  }

  public Topic getParent(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("%s/%s/parent", baseEndpoint, uuid));
  }

  public List<Locale> getTopTopicsLanguages() throws HttpException {
    return doGetRequestForObjectList(String.format("%s/top/languages", baseEndpoint), Locale.class);
  }

  public List<Topic> getTopicsOfEntity(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("%s/entity/%s", baseEndpoint, uuid));
  }

  public List<Topic> getTopicsOfFileResource(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("%s/fileresource/%s", baseEndpoint, uuid));
  }

  public boolean removeChild(UUID parentUuid, UUID childUuid) throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format("%s/%s/children/%s", baseEndpoint, parentUuid, childUuid)));
  }

  public List<Entity> saveEntities(UUID uuid, List entities) throws HttpException {
    return doPostRequestForObjectList(
        String.format("%s/%s/entities", baseEndpoint, uuid), entities, Entity.class);
  }

  public List<FileResource> saveFileResources(UUID uuid, List fileResources) throws HttpException {
    return doPostRequestForObjectList(
        String.format("%s/%s/fileresources", baseEndpoint, uuid),
        fileResources,
        FileResource.class);
  }

  public Topic saveWithParentTopic(Topic subtopic, UUID parentTopicUuid) throws HttpException {
    return doPostRequestForObject(
        String.format("%s/%s/subtopic", baseEndpoint, parentTopicUuid), subtopic);
  }
}
