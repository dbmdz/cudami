package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.Topic;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for Topic. */
public interface TopicService extends NodeService<Topic>, EntityService<Topic> {

  PageResponse<Entity> findEntities(UUID topicUuid, PageRequest pageRequest);

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
    return getTopicsOfFileResource(fileResource.getUuid());
  }

  List<Topic> getTopicsOfFileResource(UUID fileResourceUuid);

  default List<Entity> saveEntities(Topic topic, List<Entity> entities) {
    if (topic == null) {
      return null;
    }
    return setEntities(topic.getUuid(), entities);
  }

  List<Entity> setEntities(UUID topicUuid, List<Entity> entities);

  default List<FileResource> saveFileResources(Topic topic, List<FileResource> fileResources) {
    if (topic == null) {
      return null;
    }
    return setFileResources(topic.getUuid(), fileResources);
  }

  List<FileResource> setFileResources(UUID topicUuid, List<FileResource> fileResources);
}
