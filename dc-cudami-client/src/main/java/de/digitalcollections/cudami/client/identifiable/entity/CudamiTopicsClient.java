package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
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
  public SearchPageResponse<Topic> find(SearchPageRequest pageRequest) throws TechnicalException {
    // FIXME /search or not. make everywhere the same endpoint syntax please!
    return this.doGetSearchRequestForPagedObjectList(baseEndpoint, pageRequest);
  }

  public PageResponse<Topic> findSubtopics(UUID uuid, SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/%s/subtopics", baseEndpoint, uuid), searchPageRequest);
  }

  public SearchPageResponse<Topic> findTopTopics(SearchPageRequest searchPageRequest)
      throws TechnicalException {
    return doGetSearchRequestForPagedObjectList(
        String.format("%s/top", baseEndpoint), searchPageRequest);
  }

  public List<Entity> findAllEntities(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/entities/all", baseEndpoint, uuid), Entity.class);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws TechnicalException {
    return (BreadcrumbNavigation)
        doGetRequestForObject(
            String.format("%s/%s/breadcrumb", baseEndpoint, uuid), BreadcrumbNavigation.class);
  }

  public List<Topic> findChildren(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/%s/children", baseEndpoint, uuid));
  }

  public PageResponse<Topic> findChildren(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/children", baseEndpoint, uuid), pageRequest);
  }

  public PageResponse<Entity> findEntities(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/entities", baseEndpoint, uuid), pageRequest, Entity.class);
  }

  public PageResponse<FileResource> findFileResources(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/fileresources", baseEndpoint, uuid), pageRequest, FileResource.class);
  }

  public List<Locale> findLanguagesOfEntities(UUID topicUuid) throws TechnicalException {
    return this.doGetRequestForObjectList(
        String.format("%s/%s/entities/languages", baseEndpoint, topicUuid), Locale.class);
  }

  public List<Locale> findLanguagesOfFileResources(UUID topicUuid) throws TechnicalException {
    return this.doGetRequestForObjectList(
        String.format("%s/%s/fileresources/languages", baseEndpoint, topicUuid), Locale.class);
  }

  public Topic getParent(UUID uuid) throws TechnicalException {
    return doGetRequestForObject(String.format("%s/%s/parent", baseEndpoint, uuid));
  }

  public List<Locale> findTopTopicsLanguages() throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/top/languages", baseEndpoint), Locale.class);
  }

  public List<Topic> findTopicsOfEntity(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/entity/%s", baseEndpoint, uuid));
  }

  public List<Topic> findTopicsOfFileResource(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/fileresource/%s", baseEndpoint, uuid));
  }

  public boolean removeChild(UUID parentUuid, UUID childUuid) throws TechnicalException {
    return Boolean.parseBoolean(
        doDeleteRequestForString(
            String.format("%s/%s/children/%s", baseEndpoint, parentUuid, childUuid)));
  }

  public List<Entity> setEntities(UUID uuid, List entities) throws TechnicalException {
    // FIXME: should be PUT
    return doPostRequestForObjectList(
        String.format("%s/%s/entities", baseEndpoint, uuid), entities, Entity.class);
  }

  public List<FileResource> setFileResources(UUID uuid, List fileResources)
      throws TechnicalException {
    // FIXME: should be PUT
    return doPostRequestForObjectList(
        String.format("%s/%s/fileresources", baseEndpoint, uuid),
        fileResources,
        FileResource.class);
  }

  public Topic saveWithParentTopic(Topic subtopic, UUID parentTopicUuid) throws TechnicalException {
    return doPostRequestForObject(
        String.format("%s/%s/subtopic", baseEndpoint, parentTopicUuid), subtopic);
  }
}
