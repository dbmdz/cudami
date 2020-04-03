package de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.NodeService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

/** Service for Subtopic. */
public interface SubtopicService
    extends NodeService<Subtopic>, EntityPartService<Subtopic, Entity> {

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
}
