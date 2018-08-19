package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ContentNodeRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ContentNodeService;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for ContentNode handling.
 */
@Service
//@Transactional(readOnly = true)
public class ContentNodeServiceImpl extends ResourceServiceImpl<ContentNode> implements ContentNodeService<ContentNode> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeServiceImpl.class);

  @Autowired
  private LocaleService localeService;

  @Autowired
  public ContentNodeServiceImpl(ContentNodeRepository<ContentNode> repository) {
    super(repository);
  }

  @Override
  public ContentNode get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    ContentNode contentNode = repository.findOne(uuid, locale);

    // content node does not exist in requested language, so try with default locale
    if (contentNode == null) {
      contentNode = repository.findOne(uuid, localeService.getDefault());
    }

    // content node does not exist in default locale, so just return first existing language
    if (contentNode == null) {
      contentNode = repository.findOne(uuid, null);
    }

    return contentNode;
  }

  @Override
  public List<ContentNode> getChildren(ContentNode contentNode) {
    return ((NodeRepository) repository).getChildren(contentNode);
  }

  @Override
  public List<ContentNode> getChildren(UUID uuid) {
    return ((NodeRepository) repository).getChildren(uuid);
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

}
