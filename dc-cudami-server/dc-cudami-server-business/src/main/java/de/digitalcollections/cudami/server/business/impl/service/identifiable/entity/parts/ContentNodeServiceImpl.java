package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.ContentNodeService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for ContentNode handling.
 *
 * @param <E> entity type
 */
@Service
//@Transactional(readOnly = true)
public class ContentNodeServiceImpl<E extends Entity> extends EntityPartServiceImpl<ContentNode, E> implements ContentNodeService<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeServiceImpl.class);

  @Autowired
  public ContentNodeServiceImpl(ContentNodeRepository<E> repository) {
    super(repository);
  }

  @Override
  public List<ContentNode> getChildren(ContentNode contentNode) {
    return ((ContentNodeRepository) repository).getChildren(contentNode);
  }

  @Override
  public List<ContentNode> getChildren(UUID uuid) {
    return ((ContentNodeRepository) repository).getChildren(uuid);
  }

  @Override
  public LinkedHashSet<E> getEntities(ContentNode contentNode) {
    return getEntities(contentNode.getUuid());
  }

  @Override
  public LinkedHashSet<E> getEntities(UUID contentNodeUuid) {
    return ((ContentNodeRepository) repository).getEntities(contentNodeUuid);
  }

  @Override
  public ContentNode getParent(ContentNode node) {
    return getParent(node.getUuid());
  }

  @Override
  public ContentNode getParent(UUID nodeUuid) {
    return (ContentNode) ((ContentNodeRepository) repository).getParent(nodeUuid);
  }

  @Override
  public LinkedHashSet<E> saveEntities(ContentNode contentNode, LinkedHashSet<E> entities) {
    return saveEntities(contentNode.getUuid(), entities);
  }

  @Override
  public LinkedHashSet<E> saveEntities(UUID contentNodeUuid, LinkedHashSet<E> entities) {
    return ((ContentNodeRepository) repository).saveEntities(contentNodeUuid, entities);
  }

  @Override
  //  @Transactional(readOnly = false)
  public ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUuid) throws IdentifiableServiceException {
    try {
      return ((ContentNodeRepository) repository).saveWithParentContentTree(contentNode, parentContentTreeUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save top-level content node " + contentNode + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUuid) throws IdentifiableServiceException {
    try {
      return ((ContentNodeRepository) repository).saveWithParentContentNode(contentNode, parentContentNodeUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save content node " + contentNode + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(ContentNode contentNode) {
    return getFileResources(contentNode.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getFileResources(UUID contentNodeUuid) {
    return ((ContentNodeRepository) repository).getFileResources(contentNodeUuid);
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(ContentNode contentNode, LinkedHashSet<FileResource> fileResources) {
    return saveFileResources(contentNode.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveFileResources(UUID contentNodeUuid, LinkedHashSet<FileResource> fileResources) {
    return ((ContentNodeRepository) repository).saveFileResources(contentNodeUuid, fileResources);
  }
}
