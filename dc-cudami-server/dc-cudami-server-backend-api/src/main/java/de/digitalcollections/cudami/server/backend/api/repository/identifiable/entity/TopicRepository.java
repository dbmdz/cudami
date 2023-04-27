package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Topic persistence handling. */
public interface TopicRepository extends NodeRepository<Topic>, EntityRepository<Topic> {

  default PageResponse<Entity> findEntities(Topic topic, PageRequest pageRequest)
      throws RepositoryException {
    if (topic == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findEntities(topic.getUuid(), pageRequest);
  }

  public PageResponse<Entity> findEntities(UUID topicUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<FileResource> findFileResources(Topic topic, PageRequest pageRequest)
      throws RepositoryException {
    if (topic == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findFileResources(topic.getUuid(), pageRequest);
  }

  PageResponse<FileResource> findFileResources(UUID topicUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<Topic> findTopicsOfEntity(Entity entity, PageRequest pageRequest)
      throws RepositoryException {
    if (entity == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findTopicsOfEntity(entity.getUuid(), pageRequest);
  }

  PageResponse<Topic> findTopicsOfEntity(UUID entityUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<Topic> findTopicsOfFileResource(
      FileResource fileResource, PageRequest pageRequest) throws RepositoryException {
    if (fileResource == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findTopicsOfFileResource(fileResource.getUuid(), pageRequest);
  }

  PageResponse<Topic> findTopicsOfFileResource(UUID fileResourceUuid, PageRequest pageRequest)
      throws RepositoryException;

  default List<FileResource> getFileResources(Topic topic) throws RepositoryException {
    if (topic == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getFileResources(topic.getUuid());
  }

  // FIXME: replace with pagerequest method
  List<FileResource> getFileResources(UUID topicUuid) throws RepositoryException;

  default List<Locale> getLanguagesOfEntities(Topic topic) throws RepositoryException {
    if (topic == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getLanguagesOfEntities(topic.getUuid());
  }

  List<Locale> getLanguagesOfEntities(UUID topicUuid) throws RepositoryException;

  default List<Locale> getLanguagesOfFileResources(Topic topic) throws RepositoryException {
    if (topic == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getLanguagesOfFileResources(topic.getUuid());
  }

  List<Locale> getLanguagesOfFileResources(UUID topicUuid) throws RepositoryException;

  default List<Topic> getTopicsOfEntity(Entity entity) throws RepositoryException {
    if (entity == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getTopicsOfEntity(entity.getUuid());
  }

  // FIXME: replace with pagerequest method
  List<Topic> getTopicsOfEntity(UUID entityUuid) throws RepositoryException;

  default List<Topic> getTopicsOfFileResource(FileResource fileResource)
      throws RepositoryException {
    if (fileResource == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getTopicsOfFileResource(fileResource.getUuid());
  }

  // FIXME: replace with pagerequest method
  List<Topic> getTopicsOfFileResource(UUID fileResourceUuid) throws RepositoryException;

  default List<Entity> setEntities(Topic topic, List<Entity> entities) throws RepositoryException {
    if (topic == null) {
      throw new IllegalArgumentException("set failed: given object must not be null");
    }
    return setEntities(topic.getUuid(), entities);
  }

  List<Entity> setEntities(UUID topicUuid, List<Entity> entities) throws RepositoryException;

  default List<FileResource> setFileResources(Topic topic, List<FileResource> fileResources)
      throws RepositoryException {
    if (topic == null) {
      throw new IllegalArgumentException("set failed: given object must not be null");
    }
    return setFileResources(topic.getUuid(), fileResources);
  }

  List<FileResource> setFileResources(UUID topicUuid, List<FileResource> fileResources)
      throws RepositoryException;
}
