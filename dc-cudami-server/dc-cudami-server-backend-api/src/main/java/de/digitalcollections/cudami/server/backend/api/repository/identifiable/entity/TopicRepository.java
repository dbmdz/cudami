package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Topic persistence handling. */
public interface TopicRepository extends NodeRepository<Topic>, EntityRepository<Topic> {

  default boolean addEntities(Topic topic, List<Entity> entities) throws RepositoryException {
    if (topic == null || entities == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    return addEntities(topic.getUuid(), entities);
  }

  boolean addEntities(UUID topicUuid, List<Entity> entities) throws RepositoryException;

  default boolean addEntity(Topic topic, Entity entity) throws RepositoryException {
    if (topic == null || entity == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    return addEntities(topic.getUuid(), Arrays.asList(entity));
  }

  default boolean addFileResource(Topic topic, FileResource fileResource)
      throws RepositoryException {
    if (topic == null || fileResource == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    return addFileResources(topic.getUuid(), Arrays.asList(fileResource));
  }

  default boolean addFileResources(Topic topic, List<FileResource> fileResources)
      throws RepositoryException {
    if (topic == null || fileResources == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    return addFileResources(topic.getUuid(), fileResources);
  }

  boolean addFileResources(UUID topicUuid, List<FileResource> fileResources)
      throws RepositoryException;

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

  default boolean removeEntity(Topic topic, Entity entity) throws RepositoryException {
    if (topic == null || entity == null) {
      throw new IllegalArgumentException("remove failed: given objects must not be null");
    }
    return removeEntity(topic.getUuid(), entity.getUuid());
  }

  boolean removeEntity(UUID topicUuid, UUID entityUuid) throws RepositoryException;

  default boolean removeFileResource(Topic topic, FileResource fileResource)
      throws RepositoryException {
    if (topic == null || fileResource == null) {
      throw new IllegalArgumentException("remove failed: given objects must not be null");
    }
    return removeFileResource(topic.getUuid(), fileResource.getUuid());
  }

  boolean removeFileResource(UUID topicUuid, UUID fileResourceUuid) throws RepositoryException;

  default boolean setEntities(Topic topic, List<Entity> entities) throws RepositoryException {
    if (topic == null) {
      throw new IllegalArgumentException("set failed: given object must not be null");
    }
    return setEntities(topic.getUuid(), entities);
  }

  boolean setEntities(UUID topicUuid, List<Entity> entities) throws RepositoryException;

  default boolean setFileResources(Topic topic, List<FileResource> fileResources)
      throws RepositoryException {
    if (topic == null) {
      throw new IllegalArgumentException("set failed: given object must not be null");
    }
    return setFileResources(topic.getUuid(), fileResources);
  }

  boolean setFileResources(UUID topicUuid, List<FileResource> fileResources)
      throws RepositoryException;
}
