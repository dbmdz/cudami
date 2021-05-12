package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for Topic. */
public interface TopicService extends NodeService<Topic>, EntityService<Topic> {

  default List<Entity> getAllEntities(Topic topic) {
    if (topic == null) {
      return null;
    }
    return getAllEntities(topic.getUuid());
  }

  List<Entity> getAllEntities(UUID topicUuid);

  PageResponse<Entity> getEntities(UUID topicUuid, PageRequest pageRequest);

  default PageResponse<FileResource> getFileResources(Topic topic, PageRequest pageRequest) {
    if (topic == null) {
      return null;
    }
    return getFileResources(topic.getUuid(), pageRequest);
  }

  PageResponse<FileResource> getFileResources(UUID topicUuid, PageRequest pageRequest);

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
    return getTopicsOfFileResource(fileResource.getUuid());
  }

  List<Topic> getTopicsOfFileResource(UUID fileResourceUuid);

  default List<Entity> saveEntities(Topic topic, List<Entity> entities) {
    if (topic == null) {
      return null;
    }
    return saveEntities(topic.getUuid(), entities);
  }

  List<Entity> saveEntities(UUID topicUuid, List<Entity> entities);

  default PageResponse<FileResource> saveFileResources(
      Topic topic, List<FileResource> fileResources) {
    if (topic == null) {
      return null;
    }
    return saveFileResources(topic.getUuid(), fileResources);
  }

  PageResponse<FileResource> saveFileResources(UUID topicUuid, List<FileResource> fileResources);
}
