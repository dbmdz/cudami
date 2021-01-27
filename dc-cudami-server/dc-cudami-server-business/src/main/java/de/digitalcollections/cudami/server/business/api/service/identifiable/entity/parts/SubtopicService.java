package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

/** Service for Subtopic. */
public interface SubtopicService extends NodeService<Subtopic>, EntityPartService<Subtopic> {

  Subtopic addSubtopicToParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid)
      throws IdentifiableServiceException;

  Subtopic addSubtopicToParentTopic(UUID subtopicUuid, UUID parentTopicUuid)
      throws IdentifiableServiceException;

  default Integer deleteFromParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid) {
    if (subtopic == null) {
      return null;
    }
    return removeFromParentSubtopic(subtopic.getUuid(), parentSubtopicUuid);
  }

  default Integer deleteFromParentTopic(Subtopic subtopic, UUID topicUuid) {
    if (subtopic == null) {
      return null;
    }
    return removeFromParentTopic(subtopic.getUuid(), topicUuid);
  }

  default List<Entity> getEntities(Subtopic subtopic) {
    if (subtopic == null) {
      return null;
    }
    return getEntities(subtopic.getUuid());
  }

  List<Entity> getEntities(UUID subtopicUuid);

  default List<FileResource> getFileResources(Subtopic subtopic) {
    if (subtopic == null) {
      return null;
    }
    return getFileResources(subtopic.getUuid());
  }

  List<FileResource> getFileResources(UUID subtopicUuid);

  default List<Subtopic> getSubtopicsOfEntity(Entity entity) {
    if (entity == null) {
      return null;
    }
    return getSubtopicsOfEntity(entity.getUuid());
  }

  List<Subtopic> getSubtopicsOfEntity(UUID entityUuid);

  default List<Subtopic> getSubtopicsOfFileResource(FileResource fileResource) {
    if (fileResource == null) {
      return null;
    }
    return getSubtopicsOfEntity(fileResource.getUuid());
  }

  List<Subtopic> getSubtopicsOfFileResource(UUID fileResourceUuid);

  Topic getTopic(UUID subtopicUuid);

  Integer removeFromParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid);

  Integer removeFromParentTopic(UUID subtopicUuid, UUID topicUuid);

  default List<Entity> saveEntities(Subtopic subtopic, List<Entity> entities) {
    if (subtopic == null) {
      return null;
    }
    return saveEntities(subtopic.getUuid(), entities);
  }

  List<Entity> saveEntities(UUID subtopicUuid, List<Entity> entities);

  default List<FileResource> saveFileResources(
      Subtopic subtopic, List<FileResource> fileResources) {
    if (subtopic == null) {
      return null;
    }
    return saveFileResources(subtopic.getUuid(), fileResources);
  }

  List<FileResource> saveFileResources(UUID subtopicUuid, List<FileResource> fileResources);

  Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid)
      throws IdentifiableServiceException;
}
