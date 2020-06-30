package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.SubtopicRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.SubtopicService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Topic;
import de.digitalcollections.model.api.identifiable.entity.parts.Subtopic;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.entity.parts.SubtopicImpl;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Subtopic handling. */
@Service
public class SubtopicServiceImpl extends EntityPartServiceImpl<Subtopic, Entity>
    implements SubtopicService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubtopicServiceImpl.class);

  @Autowired
  public SubtopicServiceImpl(SubtopicRepository repository) {
    super(repository);
  }

  @Override
  public List<Subtopic> getChildren(Subtopic subtopic) {
    return ((SubtopicRepository) repository).getChildren(subtopic);
  }

  @Override
  public List<Subtopic> getChildren(UUID uuid) {
    return ((SubtopicRepository) repository).getChildren(uuid);
  }

  @Override
  public PageResponse<Subtopic> getChildren(UUID uuid, PageRequest pageRequest) {
    return ((NodeRepository) repository).getChildren(uuid, pageRequest);
  }

  @Override
  public List<Entity> getEntities(Subtopic subtopic) {
    return getEntities(subtopic.getUuid());
  }

  @Override
  public List<Entity> getEntities(UUID subtopicUuid) {
    return ((SubtopicRepository) repository).getEntities(subtopicUuid);
  }

  @Override
  public Subtopic getParent(Subtopic node) {
    return getParent(node.getUuid());
  }

  @Override
  public Subtopic getParent(UUID nodeUuid) {
    return (Subtopic) ((SubtopicRepository) repository).getParent(nodeUuid);
  }

  @Override
  public List<Subtopic> getSubtopicsOfEntity(UUID entityUuid) {
    return ((SubtopicRepository) repository).getSubtopicsOfEntity(entityUuid);
  }

  @Override
  public List<Subtopic> getSubtopicsOfFileResource(UUID fileResourceUuid) {
    return ((SubtopicRepository) repository).getSubtopicsOfEntity(fileResourceUuid);
  }

  @Override
  public List<Entity> saveEntities(Subtopic subtopic, List<Entity> entities) {
    return saveEntities(subtopic.getUuid(), entities);
  }

  @Override
  public List<Entity> saveEntities(UUID subtopicUuid, List<Entity> entities) {
    return ((SubtopicRepository) repository).saveEntities(subtopicUuid, entities);
  }

  @Override
  public Subtopic saveWithParentTopic(Subtopic subtopic, UUID parentTopicUuid)
      throws IdentifiableServiceException {
    try {
      return ((SubtopicRepository) repository).saveWithParentTopic(subtopic, parentTopicUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save top-level subtopic " + subtopic + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public Subtopic saveWithParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid)
      throws IdentifiableServiceException {
    try {
      return ((SubtopicRepository) repository).saveWithParentSubtopic(subtopic, parentSubtopicUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save subtopic " + subtopic + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public List<FileResource> getFileResources(Subtopic subtopic) {
    return getFileResources(subtopic.getUuid());
  }

  @Override
  public List<FileResource> getFileResources(UUID subtopicUuid) {
    return ((SubtopicRepository) repository).getFileResources(subtopicUuid);
  }

  @Override
  public List<FileResource> saveFileResources(Subtopic subtopic, List<FileResource> fileResources) {
    return saveFileResources(subtopic.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveFileResources(UUID subtopicUuid, List<FileResource> fileResources) {
    return ((SubtopicRepository) repository).saveFileResources(subtopicUuid, fileResources);
  }

  @Override
  public Integer deleteFromParentSubtopic(Subtopic subtopic, UUID parentSubtopicUuid) {
    return ((SubtopicRepository) repository).deleteFromParentSubtopic(subtopic, parentSubtopicUuid);
  }

  @Override
  public Integer deleteFromParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid) {
    return ((SubtopicRepository) repository)
        .deleteFromParentSubtopic(subtopicUuid, parentSubtopicUuid);
  }

  @Override
  public Integer deleteFromParentTopic(Subtopic subtopic, UUID topicUuid) {
    return ((SubtopicRepository) repository).deleteFromParentTopic(subtopic, topicUuid);
  }

  @Override
  public Integer deleteFromParentTopic(UUID subtopicUuid, UUID topicUuid) {
    return ((SubtopicRepository) repository).deleteFromParentTopic(subtopicUuid, topicUuid);
  }

  @Override
  public Subtopic addSubtopicToParentTopic(UUID subtopicUuid, UUID parentTopicUuid)
      throws IdentifiableServiceException {
    SubtopicImpl subtopic = new SubtopicImpl();
    subtopic.setUuid(subtopicUuid);
    return saveWithParentTopic(subtopic, parentTopicUuid);
  }

  @Override
  public Subtopic addSubtopicToParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid)
      throws IdentifiableServiceException {
    SubtopicImpl subtopic = new SubtopicImpl();
    subtopic.setUuid(subtopicUuid);
    return saveWithParentSubtopic(subtopic, parentSubtopicUuid);
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {
    return ((SubtopicRepository) repository).getBreadcrumbNavigation(nodeUuid);
  }

  @Override
  public Topic getTopic(UUID subtopicUuid) {
    UUID rootSubtopicUuid = subtopicUuid;
    Subtopic parent = getParent(subtopicUuid);
    while (parent != null) {
      rootSubtopicUuid = parent.getUuid();
      parent = getParent(parent);
    }
    // root subtopic under a topic
    return ((SubtopicRepository) repository).getTopic(rootSubtopicUuid);
  }
}
