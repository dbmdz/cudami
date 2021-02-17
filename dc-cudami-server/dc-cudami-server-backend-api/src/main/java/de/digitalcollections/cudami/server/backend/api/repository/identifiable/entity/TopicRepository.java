package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

/** Repository for Topic persistence handling. */
public interface TopicRepository extends NodeRepository<Topic>, EntityRepository<Topic> {

  default List<Entity> getEntities(Topic topic) {
    if (topic == null) {
      return null;
    }
    return getEntities(topic.getUuid());
  }

  List<Entity> getEntities(UUID topicUuid);

  default List<FileResource> getFileResources(Topic topic) {
    if (topic == null) {
      return null;
    }
    return getFileResources(topic.getUuid());
  }

  List<FileResource> getFileResources(UUID topicUuid);

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

  default List<Entity> saveEntities(Topic topic, List<Entity> entities) {
    if (topic == null) {
      return null;
    }
    return saveEntities(topic.getUuid(), entities);
  }

  List<Entity> saveEntities(UUID topicUuid, List<Entity> entities);

  default List<FileResource> saveFileResources(Topic topic, List<FileResource> fileResources) {
    if (topic == null) {
      return null;
    }
    return saveFileResources(topic.getUuid(), fileResources);
  }

  List<FileResource> saveFileResources(UUID topicUuid, List<FileResource> fileResources);
}
