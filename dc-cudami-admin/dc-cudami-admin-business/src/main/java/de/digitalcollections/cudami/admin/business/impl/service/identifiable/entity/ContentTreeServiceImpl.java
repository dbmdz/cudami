package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.ContentTreeRepository;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.ContentTreeService;
import de.digitalcollections.cudami.model.api.identifiable.entity.ContentTree;
import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import de.digitalcollections.cudami.model.api.identifiable.resource.ContentNode;
import de.digitalcollections.cudami.model.impl.identifiable.parts.TextImpl;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;

/**
 * Service for ContentTree handling.
 */
@Service
//@Transactional(readOnly = true)
public class ContentTreeServiceImpl implements ContentTreeService<ContentTree> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ContentTreeServiceImpl.class);

  @Autowired
  private ContentTreeRepository contentTreeRepository;

  @Autowired
  LocaleService localeService;

  @Override
  public long count() {
    return contentTreeRepository.count();
  }

  @Override
  public ContentTree create() {
    ContentTree contentTree = (ContentTree) contentTreeRepository.create();
    String defaultLocale = localeService.getDefault().getLanguage();
    Text label = new TextImpl(defaultLocale, "");
    contentTree.setLabel(label);
    Text description = new TextImpl(defaultLocale, "");
    contentTree.setDescription(description);
    return contentTree;
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
  @Transactional(readOnly = false)
  public ContentTree save(ContentTree contentTree, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        contentTree = (ContentTree) contentTreeRepository.save(contentTree);
      } catch (Exception e) {
        LOGGER.error("Cannot save content tree " + contentTree + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return contentTree;
  }

  @Override
  @Transactional(readOnly = false)
  public ContentTree update(ContentTree contentTree, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        contentTree = (ContentTree) contentTreeRepository.update(contentTree);
      } catch (Exception e) {
        LOGGER.error("Cannot update content tree " + contentTree + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return contentTree;
  }

//  @Override
//  public Website find(UUID uuid) {
//    return (Website) websiteRepository.find(uuid);
//  }
  public void setRepository(ContentTreeRepository contentTreeRepository) {
    this.contentTreeRepository = contentTreeRepository;
  }

  @Override
  public List<ContentNode> getRootNodes(ContentTree contentTree) {
    return contentTreeRepository.getRootNodes(contentTree);
  }
}
