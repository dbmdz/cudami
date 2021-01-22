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
public class SubtopicServiceImpl extends EntityPartServiceImpl<Subtopic>
    implements SubtopicService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SubtopicServiceImpl.class);

  @Autowired
  public SubtopicServiceImpl(SubtopicRepository repository) {
    super(repository);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<Subtopic> collections) {
    return ((NodeRepository<Subtopic>) repository).addChildren(parentUuid, collections);
  }

  @Override
  public Subtopic addSubtopicToParentSubtopic(UUID subtopicUuid, UUID parentSubtopicUuid)
      throws IdentifiableServiceException {
    SubtopicImpl subtopic = new SubtopicImpl();
    subtopic.setUuid(subtopicUuid);
    return saveWithParent(subtopic, parentSubtopicUuid);
  }

  @Override
  public Subtopic addSubtopicToParentTopic(UUID subtopicUuid, UUID parentTopicUuid)
      throws IdentifiableServiceException {
    SubtopicImpl subtopic = new SubtopicImpl();
    subtopic.setUuid(subtopicUuid);
    return saveWithParentTopic(subtopic, parentTopicUuid);
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
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {
    return ((NodeRepository<Subtopic>) repository).getBreadcrumbNavigation(nodeUuid);
  }

  @Override
  public List<Subtopic> getChildren(UUID uuid) {
    return ((SubtopicRepository) repository).getChildren(uuid);
  }

  @Override
  public PageResponse<Subtopic> getChildren(UUID uuid, PageRequest pageRequest) {
    return ((NodeRepository<Subtopic>) repository).getChildren(uuid, pageRequest);
  }

  @Override
  public List<Entity> getEntities(UUID subtopicUuid) {
    return ((SubtopicRepository) repository).getEntities(subtopicUuid);
  }

  @Override
  public List<FileResource> getFileResources(UUID subtopicUuid) {
    return ((SubtopicRepository) repository).getFileResources(subtopicUuid);
  }

  @Override
  public Subtopic getParent(UUID nodeUuid) {
    return ((SubtopicRepository) repository).getParent(nodeUuid);
  }

  @Override
  public List<Subtopic> getParents(UUID uuid) {
    return ((NodeRepository<Subtopic>) repository).getParents(uuid);
  }

  @Override
  public PageResponse<Subtopic> getRootNodes(PageRequest pageRequest) {
    return ((NodeRepository<Subtopic>) repository).getRootNodes(pageRequest);
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

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    return ((NodeRepository<Subtopic>) repository).removeChild(parentUuid, childUuid);
  }

  @Override
  public List<Entity> saveEntities(UUID subtopicUuid, List<Entity> entities) {
    return ((SubtopicRepository) repository).saveEntities(subtopicUuid, entities);
  }

  @Override
  public List<FileResource> saveFileResources(UUID subtopicUuid, List<FileResource> fileResources) {
    return ((SubtopicRepository) repository).saveFileResources(subtopicUuid, fileResources);
  }

  @Override
  public Subtopic saveWithParent(Subtopic child, UUID parentSubtopicUuid)
      throws IdentifiableServiceException {
    try {
      return ((SubtopicRepository) repository).saveWithParent(child, parentSubtopicUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save subtopic " + child + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
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
  public boolean updateChildrenOrder(UUID parentUuid, List<Subtopic> children) {
    return ((NodeRepository<Subtopic>) repository).updateChildrenOrder(parentUuid, children);
  }
}
