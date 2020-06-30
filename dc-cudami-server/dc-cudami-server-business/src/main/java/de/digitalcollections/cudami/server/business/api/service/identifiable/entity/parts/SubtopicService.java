package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for Subtopic. */
public interface SubtopicService
    extends NodeService<Subtopic>, EntityPartService<Subtopic, Entity> {

  @Override
  Subtopic get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  List<Entity> getEntities(Subtopic subtopic);

  List<Entity> getEntities(UUID subtopicUuid);

  List<Entity> saveEntities(Subtopic subtopic, List<Entity> entities);

  List<Entity> saveEntities(UUID subtopicUuid, List<Entity> entities);

  List<FileResource> getFileResources(Subtopic subtopic);

  List<FileResource> getFileResources(UUID subtopicUuid);

  List<FileResource> saveFileResources(Subtopic subtopic, List<FileResource> fileResources);

  List<FileResource> saveFileResources(UUID subtopicUuid, List<FileResource> fileResources);

  Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid)
      throws IdentifiableServiceException;

  Subtopic saveWithParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid)
      throws IdentifiableServiceException;

  List<Subtopic> getSubtopicsOfEntity(UUID entityUuid);

  List<Subtopic> getSubtopicsOfFileResource(UUID fileResourceUuid);

  Integer deleteFromParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid);

  Integer deleteFromParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid);

  Integer deleteFromParentTopic(Subtopic subtopic, UUID topicUuid);

  Integer deleteFromParentTopic(UUID subtopicUuid, UUID topicUuid);

  Subtopic addSubtopicToParentTopic(UUID subtopicUuid, UUID parentTopicUuid)
      throws IdentifiableServiceException;

  Subtopic addSubtopicToParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid)
      throws IdentifiableServiceException;

  Topic getTopic(UUID subtopicUuid);
}
