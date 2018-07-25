package de.digitalcollections.cudami.admin.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.ContentNodeRepository;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.ContentNodeService;
import de.digitalcollections.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
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
public class ContentNodeServiceImpl implements ContentNodeService<ContentNode> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentNodeServiceImpl.class);

  @Autowired
  private ContentNodeRepository contentNodeRepository;

  @Autowired
  LocaleService localeService;

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public ContentNode create() {
    ContentNode contentNode = (ContentNode) contentNodeRepository.create();
    return contentNode;
  }

  @Override
  public PageResponse<ContentNode> find(PageRequest pageRequest) {
    return contentNodeRepository.find(pageRequest);
  }

  @Override
  public ContentNode get(UUID uuid) {
    return (ContentNode) contentNodeRepository.findOne(uuid);
  }

  @Override
  public ContentNode save(ContentNode contentNode, Errors results) throws IdentifiableServiceException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public ContentNode saveWithParentContentTree(ContentNode contentNode, UUID parentContentTreeUUID, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        contentNode = (ContentNode) contentNodeRepository.saveWithParentContentTree(contentNode, parentContentTreeUUID);
      } catch (Exception e) {
        LOGGER.error("Cannot save top-level content node " + contentNode + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return contentNode;
  }

  @Override
  public ContentNode saveWithParentContentNode(ContentNode contentNode, UUID parentContentNodeUUID, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        contentNode = (ContentNode) contentNodeRepository.saveWithParentContentNode(contentNode, parentContentNodeUUID);
      } catch (Exception e) {
        LOGGER.error("Cannot save content node " + contentNode + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return contentNode;
  }

  public void setContentNodeRepository(ContentNodeRepository contentNodeRepository) {
    this.contentNodeRepository = contentNodeRepository;
  }

  @Override
  public ContentNode update(ContentNode contentNode, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        contentNode = (ContentNode) contentNodeRepository.update(contentNode);
      } catch (Exception e) {
        LOGGER.error("Cannot update content node " + contentNode + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return contentNode;
  }
}
