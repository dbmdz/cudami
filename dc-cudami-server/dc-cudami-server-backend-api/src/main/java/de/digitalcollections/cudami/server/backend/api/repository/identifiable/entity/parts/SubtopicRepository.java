package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Subtopic persistence handling.
 * @param <S> instance of subtopic implementation
 */
public interface SubtopicRepository<S extends Subtopic>
        extends NodeRepository<S>, EntityPartRepository<S> {

  @Override
  default List<S> getChildren(S subtopic) {
    if (subtopic == null) {
      return null;
    }
    return getChildren(subtopic.getUuid());
  }

  default List<Entity> getEntities(S subtopic) {
    if (subtopic == null) {
      return null;
    }
    return getEntities(subtopic.getUuid());
  }

  List<Entity> getEntities(UUID subtopicUuid);

  default List<Entity> saveEntities(S subtopic, List<Entity> entities) {
    if (subtopic == null) {
      return null;
    }
    return saveEntities(subtopic.getUuid(), entities);
  }

  List<Entity> saveEntities(UUID subtopicUuid, List<Entity> entities);

  default List<FileResource> getFileResources(S subtopic) {
    if (subtopic == null) {
      return null;
    }
    return getFileResources(subtopic.getUuid());
  }

  List<FileResource> getFileResources(UUID subtopicUuid);

  default List<FileResource> saveFileResources(
          S subtopic, List<FileResource> fileResources) {
    if (subtopic == null) {
      return null;
    }
    return saveFileResources(subtopic.getUuid(), fileResources);
  }

  List<FileResource> saveFileResources(UUID subtopicUuid, List<FileResource> fileResources);

  S saveWithParentTopic(S subtopic, UUID parentTopicUuid);

  S saveWithParentSubtopic(S subtopic, UUID parentSubtopicUuid);

  default List<S> getSubtopicsOfEntity(Entity entity) {
    if (entity == null) {
      return null;
    }
    return getSubtopicsOfEntity(entity.getUuid());
  }

  List<S> getSubtopicsOfEntity(UUID entityUuid);

  default List<S> getSubtopicsOfFileResource(FileResource fileResource) {
    if (fileResource == null) {
      return null;
    }
    return getSubtopicsOfEntity(fileResource.getUuid());
  }

  List<S> getSubtopicsOfFileResource(UUID fileResourceUuid);

  default Integer deleteFromParentSubtopic(S subtopic, UUID parentSubtopicUuid) {
    if (subtopic == null) {
      return null;
    }
    return deleteFromParentSubtopic(subtopic.getUuid(), parentSubtopicUuid);
  }

  Integer deleteFromParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid);

  default Integer deleteFromParentTopic(S subtopic, UUID topicUuid) {
    if (subtopic == null) {
      return null;
    }
    return deleteFromParentTopic(subtopic.getUuid(), topicUuid);
  }

  Integer deleteFromParentTopic(UUID subtopicUuid, UUID topicUuid);

  /**
   * @param rootSubtopicUuid uuid of a subtopic (subtopic must be a top level subtopic under a topic)
   * @return the topic the given root-subtopic belongs to (subtopic is top level subtopic)
   */
  Topic getTopic(UUID rootSubtopicUuid);
}
