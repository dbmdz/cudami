package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts.ContentNodeRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.parts.ContentNodeService;
import de.digitalcollections.cudami.admin.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.parts.ContentNode;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

/**
 * Service for ContentNode handling.
 */
@Service
//@Transactional(readOnly = true)
public class ContentNodeServiceImpl extends IdentifiableServiceImpl<ContentNode> implements ContentNodeService<ContentNode> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeServiceImpl.class);

  @Autowired
  public ContentNodeServiceImpl(ContentNodeRepository<ContentNode> repository) {
    super(repository);
  }

  @Override
  public ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUUID, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        contentNode = (ContentNode) ((ContentNodeRepository) repository).saveWithParentContentTree(contentNode, parentContentTreeUUID);
      } catch (Exception e) {
        LOGGER.error("Cannot save top-level content node " + contentNode + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    // FIXME: what if results has errors? throw exception?
    return contentNode;
  }

  @Override
  public ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUUID, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        contentNode = (ContentNode) ((ContentNodeRepository) repository).saveWithParentContentNode(contentNode, parentContentNodeUUID);
      } catch (Exception e) {
        LOGGER.error("Cannot save content node " + contentNode + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    // FIXME: what if results has errors? throw exception?
    return contentNode;
  }

  @Override
  public List<ContentNode> getChildren(ContentNode node) {
    return ((NodeRepository) repository).getChildren(node);
  }

  @Override
  public List<ContentNode> getChildren(UUID uuid) {
    return ((NodeRepository) repository).getChildren(uuid);
  }

  @Override
  public List<Identifiable> getIdentifiables(ContentNode contentNode) {
    return ((ContentNodeRepository) repository).getIdentifiables(contentNode);
  }
}
