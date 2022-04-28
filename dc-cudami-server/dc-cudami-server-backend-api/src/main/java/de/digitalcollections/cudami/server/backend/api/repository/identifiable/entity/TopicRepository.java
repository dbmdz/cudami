package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Topic persistence handling. */
public interface TopicRepository extends NodeRepository<Topic>, EntityRepository<Topic> {

  default List<Entity> getAllEntities(Topic topic) {
    if (topic == null) {
      return null;
    }
    return getEntities(topic.getUuid());
  }

  List<Entity> getEntities(UUID topicUuid);

  public PageResponse<Entity> findEntities(UUID topicUuid, PageRequest pageRequest);

  default List<FileResource> getFileResources(Topic topic) {
    if (topic == null) {
      return null;
    }
    return getFileResources(topic.getUuid());
  }

  List<FileResource> getFileResources(UUID topicUuid);

  PageResponse<FileResource> findFileResources(UUID topicUuid, PageRequest pageRequest);

  List<Locale> getLanguagesOfEntities(UUID topicUuid);

  List<Locale> getLanguagesOfFileResources(UUID topicUuid);

  default List<Topic> getTopicsOfEntity(Entity entity) {
    if (entity == null) {
      return null;
    }
    return getTopicsOfEntity(entity.getUuid());
  }

  List<Topic> getTopicsOfEntity(UUID entityUuid);

  default List<Topic> getTopicsOfFileResource(FileResource fileResource) {
    if (fileResource == null) {
      return null;
    }
    return getTopicsOfEntity(fileResource.getUuid());
  }

  List<Topic> getTopicsOfFileResource(UUID fileResourceUuid);

  default List<Entity> setEntities(Topic topic, List<Entity> entities) {
    if (topic == null) {
      return null;
    }
    return setEntities(topic.getUuid(), entities);
  }

  List<Entity> setEntities(UUID topicUuid, List<Entity> entities);

  default List<FileResource> setFileResources(Topic topic, List<FileResource> fileResources) {
    if (topic == null) {
      return null;
    }
    return setFileResources(topic.getUuid(), fileResources);
  }

  List<FileResource> setFileResources(UUID topicUuid, List<FileResource> fileResources);
}
