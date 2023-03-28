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
      return null;
    }
    return findEntities(topic.getUuid(), pageRequest);
  }

  public PageResponse<Entity> findEntities(UUID topicUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<FileResource> findFileResources(Topic topic, PageRequest pageRequest)
      throws RepositoryException {
    if (topic == null) {
      return null;
    }
    return findFileResources(topic.getUuid(), pageRequest);
  }

  PageResponse<FileResource> findFileResources(UUID topicUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<Topic> findTopicsOfEntity(Entity entity, PageRequest pageRequest) {
    if (entity == null) {
      return null;
    }
    return findTopicsOfEntity(entity.getUuid(), pageRequest);
  }

  // FIXME: replace with pagerequest method
  List<Topic> findTopicsOfEntity(UUID entityUuid) throws RepositoryException;

  PageResponse<Topic> findTopicsOfEntity(UUID entityUuid, PageRequest pageRequest);

  default PageResponse<Topic> findTopicsOfFileResource(
      FileResource fileResource, PageRequest pageRequest) {
    if (fileResource == null) {
      return null;
    }
    return findTopicsOfFileResource(fileResource.getUuid(), pageRequest);
  }

  // FIXME: replace with pagerequest method
  List<Topic> findTopicsOfFileResource(UUID fileResourceUuid) throws RepositoryException;

  PageResponse<Topic> findTopicsOfFileResource(UUID fileResourceUuid, PageRequest pageRequest);

  List<Locale> getLanguagesOfEntities(UUID topicUuid);

  List<Locale> getLanguagesOfFileResources(UUID topicUuid);

  default List<Entity> setEntities(Topic topic, List<Entity> entities) throws RepositoryException {
    if (topic == null) {
      return null;
    }
    return setEntities(topic.getUuid(), entities);
  }

  List<Entity> setEntities(UUID topicUuid, List<Entity> entities) throws RepositoryException;

  default List<FileResource> setFileResources(Topic topic, List<FileResource> fileResources)
      throws RepositoryException {
    if (topic == null) {
      return null;
    }
    return setFileResources(topic.getUuid(), fileResources);
  }

  List<FileResource> setFileResources(UUID topicUuid, List<FileResource> fileResources)
      throws RepositoryException;

  // FIXME: replace with pagerequest method
  List<FileResource> getFileResources(UUID topicUuid) throws RepositoryException;
}
