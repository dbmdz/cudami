package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ContentTreeService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for ContentTree handling.
 */
@Service
//@Transactional(readOnly = true)
public class ContentTreeServiceImpl implements ContentTreeService<ContentTree> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentTreeServiceImpl.class);

  @Autowired
  private ContentTreeRepository contentTreeRepository;

  @Override
  public long count() {
    return contentTreeRepository.count();
  }

  @Override
  public ContentTree create() {
    return (ContentTree) contentTreeRepository.create();
  }

  @Override
  public PageResponse<ContentTree> find(PageRequest pageRequest) {
    return contentTreeRepository.find(pageRequest);
  }

  @Override
  public ContentTree get(UUID uuid) {
    return (ContentTree) contentTreeRepository.findOne(uuid);
  }

  @Override
  public List<ContentNode> getRootNodes(ContentTree contentTree) {
    return contentTreeRepository.getRootNodes(contentTree);
  }

  @Override
  //  @Transactional(readOnly = false)
  public ContentTree save(ContentTree contentTree) throws IdentifiableServiceException {
    try {
      return (ContentTree) contentTreeRepository.save(contentTree);
    } catch (Exception e) {
      LOGGER.error("Cannot save content tree " + contentTree + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public ContentTree update(ContentTree contentTree) throws IdentifiableServiceException {
    try {
      return (ContentTree) contentTreeRepository.update(contentTree);
    } catch (Exception e) {
      LOGGER.error("Cannot update content tree " + contentTree + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  public void setRepository(ContentTreeRepository contentTreeRepository) {
    this.contentTreeRepository = contentTreeRepository;
  }
}
