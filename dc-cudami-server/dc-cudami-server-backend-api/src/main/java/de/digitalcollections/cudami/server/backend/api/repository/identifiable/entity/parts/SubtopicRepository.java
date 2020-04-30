package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

/** Repository for Subtopic persistence handling. */
public interface SubtopicRepository
    extends NodeRepository<Subtopic>, EntityPartRepository<Subtopic, Entity> {

  @Override
  default List<Subtopic> getChildren(Subtopic subtopic) {
    if (subtopic == null) {
      return null;
    }
    return getChildren(subtopic.getUuid());
  }

  default List<Entity> getEntities(Subtopic subtopic) {
    if (subtopic == null) {
      return null;
    }
    return getEntities(subtopic.getUuid());
  }

  List<Entity> getEntities(UUID subtopicUuid);

  default List<Entity> saveEntities(Subtopic subtopic, List<Entity> entities) {
    if (subtopic == null) {
      return null;
    }
    return saveEntities(subtopic.getUuid(), entities);
  }

  List<Entity> saveEntities(UUID subtopicUuid, List<Entity> entities);

  default List<FileResource> getFileResources(Subtopic subtopic) {
    if (subtopic == null) {
      return null;
    }
    return getFileResources(subtopic.getUuid());
  }

  List<FileResource> getFileResources(UUID subtopicUuid);

  default List<FileResource> saveFileResources(
      Subtopic subtopic, List<FileResource> fileResources) {
    if (subtopic == null) {
      return null;
    }
    return saveFileResources(subtopic.getUuid(), fileResources);
  }

  List<FileResource> saveFileResources(UUID subtopicUuid, List<FileResource> fileResources);

  Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid);

  Subtopic saveWithParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid);

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

  default Integer deleteFromParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid) {
    if (subtopic == null) {
      return null;
    }
    return deleteFromParentSubtopic(subtopic.getUuid(), parentSubtopicUuid);
  }

  Integer deleteFromParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid);

  default Integer deleteFromParentTopic(Subtopic subtopic, UUID topicUuid) {
    if (subtopic == null) {
      return null;
    }
    return deleteFromParentTopic(subtopic.getUuid(), topicUuid);
  }

  Integer deleteFromParentTopic(UUID subtopicUuid, UUID topicUuid);
}
