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

  List<Entity> getEntities(Subtopic subtopic);

  List<Entity> getEntities(UUID subtopicUuid);

  List<Entity> saveEntities(Subtopic subtopic, List<Entity> entities);

  List<Entity> saveEntities(UUID subtopicUuid, List<Entity> entities);

  List<FileResource> getFileResources(Subtopic subtopic);

  List<FileResource> getFileResources(UUID subtopicUuid);

  List<FileResource> saveFileResources(Subtopic subtopic, List<FileResource> fileResources);

  List<FileResource> saveFileResources(UUID subtopicUuid, List<FileResource> fileResources);

  Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid);

  Subtopic saveWithParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid);

  List<Subtopic> getSubtopicsOfEntity(UUID entityUuid);

  List<Subtopic> getSubtopicsOfFileResource(FileResource fileResource);

  List<Subtopic> getSubtopicsOfFileResource(UUID fileResourceUuid);

  Integer deleteFromParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid);

  Integer deleteFromParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid);

  Integer deleteFromParentTopic(Subtopic subtopic, UUID topicUuid);

  Integer deleteFromParentTopic(UUID subtopicUuid, UUID topicUuid);
}
