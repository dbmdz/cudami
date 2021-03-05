package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.cudami.client.CudamiBaseClient;
import de.digitalcollections.cudami.client.exceptions.HttpException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
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
    return Long.parseLong(doGetRequestForString("/latest/topics/count"));
  }

  public Topic create() {
    return new Topic();
  }

  public PageResponse<Topic> find(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/topics", pageRequest);
  }

  public Topic findOne(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/topics/%s", uuid));
  }

  public Topic findOne(UUID uuid, String locale) throws HttpException {
    return doGetRequestForObject(String.format("/latest/topics/%s?locale=%s", uuid, locale));
  }

  public Topic findOneByIdentifier(String namespace, String id) throws HttpException {
    return doGetRequestForObject(
        String.format("/latest/topics/identifier/%s:%s.json", namespace, id));
  }

  public Topic findOneByRefId(long refId) throws HttpException {
    return doGetRequestForObject(String.format("/latest/topics/%d", refId));
  }

  public PageResponse<Topic> findTopTopics(PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList("/latest/topics/top", pageRequest);
  }

  public List<Entity> getAllEntities(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/topics/%s/entities/all", uuid), Entity.class);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws HttpException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("/latest/topics/%s/breadcrumb", uuid), BreadcrumbNavigation.class);
  }

  public List<Topic> getChildren(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/topics/%s/children", uuid));
  }

  public PageResponse<Topic> getChildren(UUID uuid, PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/topics/%s/children", uuid), pageRequest);
  }

  public PageResponse<Entity> getEntities(UUID uuid, PageRequest pageRequest) throws HttpException {
    return doGetRequestForPagedObjectList(
        String.format("/latest/topics/%s/entities", uuid), pageRequest, Entity.class);
  }

  public List<FileResource> getFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/topics/%s/fileresources", uuid), FileResource.class);
  }

  public Topic getParent(UUID uuid) throws HttpException {
    return doGetRequestForObject(String.format("/latest/topics/%s/parent", uuid));
  }

  public List<FileResource> getRelatedFileResources(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(
        String.format("/latest/entities/%s/related/fileresources", uuid), FileResource.class);
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
    return doGetRequestForObjectList("/latest/topics/top/languages", Locale.class);
  }

  public List<Topic> getTopicsOfEntity(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/topics/entity/%s", uuid));
  }

  public List<Topic> getTopicsOfFileResource(UUID uuid) throws HttpException {
    return doGetRequestForObjectList(String.format("/latest/topics/fileresource/%s", uuid));
  }

  public boolean removeChild(UUID parentUuid, UUID childUuid) throws HttpException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format("/latest/topics/%s/children/%s", parentUuid, childUuid)));
  }

  public Topic save(Topic topic) throws HttpException {
    return doPostRequestForObject("/latest/topics", topic);
  }

  public List<Entity> saveEntities(UUID uuid, List entities) throws HttpException {
    return doPostRequestForObjectList(
        String.format("/latest/topics/%s/entities", uuid), entities, Entity.class);
  }

  public List<FileResource> saveFileResources(UUID uuid, List fileResources) throws HttpException {
    return doPostRequestForObjectList(
        String.format("/latest/topics/%s/fileresources", uuid), fileResources, FileResource.class);
  }

  public Topic saveWithParentTopic(Topic subtopic, UUID parentTopicUuid) throws HttpException {
    return doPostRequestForObject(
        String.format("/latest/topics/%s/subtopic", parentTopicUuid), subtopic);
  }

  public Topic update(UUID uuid, Topic topic) throws HttpException {
    return doPutRequestForObject(String.format("/latest/topics/%s", uuid), topic);
  }
}
