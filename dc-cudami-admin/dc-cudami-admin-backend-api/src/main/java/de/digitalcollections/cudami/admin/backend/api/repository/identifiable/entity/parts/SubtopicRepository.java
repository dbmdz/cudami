package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.NodeRepository;
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

  List<Subtopic> getSubtopicsOfEntity(Entity entity);

  List<Subtopic> getSubtopicsOfEntity(UUID entityUuid);

  List<Subtopic> getSubtopicsOfFileResource(FileResource fileResource);

  List<Subtopic> getSubtopicsOfFileResource(UUID fileResourceUuid);
}
