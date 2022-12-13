package de.digitalcollections.cudami.client.identifiable.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.exception.http.client.ResourceNotFoundException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class CudamiTopicsClient extends CudamiEntitiesClient<Topic> {

  public CudamiTopicsClient(HttpClient http, String serverUrl, ObjectMapper mapper) {
    super(http, serverUrl, Topic.class, mapper, API_VERSION_PREFIX + "/topics");
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

  public PageResponse<Topic> findSubtopics(UUID uuid, PageRequest pageRequest)
      throws TechnicalException {
    return doGetRequestForPagedObjectList(
        String.format("%s/%s/subtopics", baseEndpoint, uuid), pageRequest);
  }

  public PageResponse<Topic> findTopTopics(PageRequest pageRequest) throws TechnicalException {
    return doGetRequestForPagedObjectList(String.format("%s/top", baseEndpoint), pageRequest);
  }

  public List<Entity> getAllEntities(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(
        String.format("%s/%s/entities/all", baseEndpoint, uuid), Entity.class);
  }

  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) throws TechnicalException {
    try {
      return (BreadcrumbNavigation)
          doGetRequestForObject(
              String.format("%s/%s/breadcrumb", baseEndpoint, uuid), BreadcrumbNavigation.class);
    } catch (ResourceNotFoundException e) {
      return null;
    }
  }

  public List<Topic> getChildren(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/%s/children", baseEndpoint, uuid));
  }

  public List<Locale> getLanguagesOfEntities(UUID topicUuid) throws TechnicalException {
    return this.doGetRequestForObjectList(
        String.format("%s/%s/entities/languages", baseEndpoint, topicUuid), Locale.class);
  }

  public List<Locale> getLanguagesOfFileResources(UUID topicUuid) throws TechnicalException {
    return this.doGetRequestForObjectList(
        String.format("%s/%s/fileresources/languages", baseEndpoint, topicUuid), Locale.class);
  }

  public List<Locale> getLanguagesOfTopTopics() throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/top/languages", baseEndpoint), Locale.class);
  }

  public Topic getParent(UUID uuid) throws TechnicalException {
    return doGetRequestForObject(String.format("%s/%s/parent", baseEndpoint, uuid));
  }

  public List<Topic> getTopicsOfEntity(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/entity/%s", baseEndpoint, uuid));
  }

  public List<Topic> getTopicsOfFileResource(UUID uuid) throws TechnicalException {
    return doGetRequestForObjectList(String.format("%s/fileresource/%s", baseEndpoint, uuid));
  }

  public boolean removeChild(UUID parentUuid, UUID childUuid) throws TechnicalException {
    try {
      doDeleteRequestForString(
          String.format("%s/%s/children/%s", baseEndpoint, parentUuid, childUuid));
    } catch (ResourceNotFoundException e) {
      return false;
    }
    return true;
  }

  public Topic saveWithParentTopic(Topic subtopic, UUID parentTopicUuid) throws TechnicalException {
    try {
      return doPostRequestForObject(
          String.format("%s/%s/subtopic", baseEndpoint, parentTopicUuid), subtopic);
    } catch (ResourceNotFoundException e) {
      return null;
    }
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
}
