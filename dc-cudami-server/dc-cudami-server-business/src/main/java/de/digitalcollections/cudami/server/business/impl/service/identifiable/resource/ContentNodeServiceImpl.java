package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ContentNodeRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ContentNodeService;
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
public class ContentNodeServiceImpl implements ContentNodeService<ContentNode> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeServiceImpl.class);

  @Autowired
  private ContentNodeRepository<ContentNode> contentNodeRepository;

  @Autowired
  private LocaleService localeService;

  @Override
  public long count() {
    return contentNodeRepository.count();
  }

  @Override
  public ContentNode create() {
    return contentNodeRepository.create();
  }

  @Override
  public PageResponse<ContentNode> find(PageRequest pageRequest) {
    return contentNodeRepository.find(pageRequest);
  }

  @Override
  public ContentNode get(UUID uuid) {
    return contentNodeRepository.findOne(uuid);
  }

  @Override
  public ContentNode get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    ContentNode contentNode = contentNodeRepository.findOne(uuid, locale);

    // content node does not exist in requested language, so try with default locale
    if (contentNode == null) {
      contentNode = contentNodeRepository.findOne(uuid, localeService.getDefault());
    }

    // content node does not exist in default locale, so just return first existing language
    if (contentNode == null) {
      contentNode = contentNodeRepository.findOne(uuid, null);
    }

    return contentNode;
  }

  @Override
  public List<ContentNode> getSubNodes(ContentNode contentNode) {
    return contentNodeRepository.getSubNodes(contentNode);
  }

  @Override
  public ContentNode save(ContentNode contentNode) throws IdentifiableServiceException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  //  @Transactional(readOnly = false)
  public ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUuid) throws IdentifiableServiceException {
    try {
      return contentNodeRepository.saveWithParentContentTree(contentNode, parentContentTreeUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save top-level content node " + contentNode + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUuid) throws IdentifiableServiceException {
    try {
      return contentNodeRepository.saveWithParentContentNode(contentNode, parentContentNodeUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save content node " + contentNode + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public ContentNode update(ContentNode contentNode) throws IdentifiableServiceException {
    try {
      return contentNodeRepository.update(contentNode);
    } catch (Exception e) {
      LOGGER.error("Cannot update content node " + contentNode + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  public void setRepository(ContentNodeRepository contentNodeRepository) {
    this.contentNodeRepository = contentNodeRepository;
  }
}
