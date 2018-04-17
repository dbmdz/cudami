package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Website handling.
 */
@Service
//@Transactional(readOnly = true)
public class WebsiteServiceImpl implements WebsiteService<Website> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteServiceImpl.class);

  @Autowired
  private WebsiteRepository websiteRepository;

  @Override
  public long count() {
    return websiteRepository.count();
  }

  @Override
  public Website create() {
    return (Website) websiteRepository.create();
  }

  @Override
  public PageResponse<Website> find(PageRequest pageRequest) {
    return websiteRepository.find(pageRequest);
  }

  @Override
  public Website get(UUID uuid) {
    return (Website) websiteRepository.findOne(uuid);
  }

  @Override
  public List<Webpage> getRootPages(Website website) {
    return websiteRepository.getRootPages(website);
  }

  @Override
  //  @Transactional(readOnly = false)
  public Website save(Website website) throws IdentifiableServiceException {
    try {
      return (Website) websiteRepository.save(website);
    } catch (Exception e) {
      LOGGER.error("Cannot save website " + website + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public Website update(Website website) throws IdentifiableServiceException {
    try {
      return (Website) websiteRepository.update(website);
    } catch (Exception e) {
      LOGGER.error("Cannot update website " + website + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

//  @Override
//  public Website find(UUID uuid) {
//    return (Website) websiteRepository.find(uuid);
//  }
  public void setRepository(WebsiteRepository websiteRepository) {
    this.websiteRepository = websiteRepository;
  }
}
