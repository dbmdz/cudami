package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
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

public class CudamiTopicsClient extends CudamiBaseClient<Topic> {

  public CudamiTopicsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Topic.class, mapper);
  }

  public long count() throws HttpException {
    return Long.parseLong(doGetRequestForString("/v5/topics/count"));
  }

  public Topic create() {
    return new Topic();
  }

  public PageResponse<Topic> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/topics", pageRequest);
  }

  public SearchPageResponse<Topic> find(SearchPageRequest pageRequest) throws HttpException {
    return this.doGetSearchRequestForPagedObjectList("/v5/topics", pageRequest);
  }

  public Topic findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/topics/%s", uuid));
  }

  public Topic findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/v5/topics/%s?locale=%s", uuid, locale));
  }

  public Topic findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(String.format("/v5/topics/identifier/%s:%s.json", namespace, id));
  }

  public Topic findOneByRefId(long refId) throws HttpException {
    return doGetRequestForObject(String.format("/v5/topics/%d", refId));
  }

  public PageResponse<Topic> findSubtopics(UUID uuid, SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList(
        String.format("/v5/topics/%s/subtopics", uuid), searchPageRequest);
  }

  public SearchPageResponse<Topic> findTopCollections(SearchPageRequest searchPageRequest)
      throws HttpException {
    return doGetSearchRequestForPagedObjectList("/v5/topics/top", searchPageRequest);
  }

  public PageResponse<Topic> findTopTopics(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/v5/topics/top", pageRequest);
  }

  public List<Entity> getAllEntities(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/topics/%s/entities/all", uuid), Entity.class);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/v5/topics/%s/breadcrumb", uuid), BreadcrumbNavigation.class);
  }

  public List<Topic> getChildren(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v5/topics/%s/children", uuid));
  }

  public PageResponse<Topic> getChildren(UUID uuid, PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/topics/%s/children", uuid), pageRequest);
  }

  public PageResponse<Entity> getEntities(UUID uuid, PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/topics/%s/entities", uuid), pageRequest, Entity.class);
  }

  public PageResponse<FileResource> getFileResources(UUID uuid, PageRequest pageRequest)
      throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/v5/topics/%s/fileresources", uuid), pageRequest, FileResource.class);
  }

  public List<Locale> getLanguagesOfEntities(UUID topicUuid) throws HttpException {
    return this.doGetRequestForObjectList(
        String.format("/v5/topics/%s/entities/languages", topicUuid), Locale.class);
  }

  public List<Locale> getLanguagesOfFileResources(UUID topicUuid) throws HttpException {
    return this.doGetRequestForObjectList(
        String.format("/v5/topics/%s/fileresources/languages", topicUuid), Locale.class);
  }

  public Topic getParent(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/v5/topics/%s/parent", uuid));
  }

  public List<FileResource> getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/v5/entities/%s/related/fileresources", uuid), FileResource.class);
  }

  /**
   * @param uuid
   * @return
   * @throws HttpException
   * @deprecated use getChildren(uuid)
   */
  @Deprecated
  public List<Topic> getSubtopics(UUID uuid) throws HttpException {
    return getChildren(uuid);
  }

  public List<Locale> getTopTopicsLanguages() throws HttpException {
    return doGetRequestForObjectList("/v5/topics/top/languages", Locale.class);
  }

  public List<Topic> getTopicsOfEntity(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v5/topics/entity/%s", uuid));
  }

  public List<Topic> getTopicsOfFileResource(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/v5/topics/fileresource/%s", uuid));
  }

  public boolean removeChild(UUID parentUuid, UUID childUuid) throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format("/v5/topics/%s/children/%s", parentUuid, childUuid)));
  }

  public Topic save(Topic topic) throws HttpException {
    return doPostRequestForObject("/v5/topics", topic);
  }

  public List<Entity> saveEntities(UUID uuid, List entities) throws HttpException {
    return doPostRequestForObjectList(
        String.format("/v5/topics/%s/entities", uuid), entities, Entity.class);
  }

  public List<FileResource> saveFileResources(UUID uuid, List fileResources) throws HttpException {
    return doPostRequestForObjectList(
        String.format("/v5/topics/%s/fileresources", uuid), fileResources, FileResource.class);
  }

  public Topic saveWithParentTopic(Topic subtopic, UUID parentTopicUuid) throws HttpException {
    return doPostRequestForObject(
        String.format("/v5/topics/%s/subtopic", parentTopicUuid), subtopic);
  }

  public Topic update(UUID uuid, Topic topic) throws HttpException {
    return doPutRequestForObject(String.format("/v5/topics/%s", uuid), topic);
  }
}
