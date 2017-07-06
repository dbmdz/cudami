package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.cudami.model.api.entity.ContentNode;
import de.digitalcollections.cudami.model.api.entity.Website;
import de.digitalcollections.cudami.server.backend.api.repository.WebsiteRepository;
import de.digitalcollections.cudami.server.business.api.service.WebsiteService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.WebsiteServiceException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for Website handling.
 */
@Service
@Transactional(readOnly = true)
public class WebsiteServiceImpl implements WebsiteService<Website, Long> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteServiceImpl.class);

  @Autowired
  private WebsiteRepository websiteRepository;

  @Override
  public Website create() {
    return websiteRepository.create();
  }

  @Override
  public Website get(Long id) throws WebsiteServiceException {
    Website website = (Website) websiteRepository.findOne(id);
    if (website == null) {
      return null;
    }
    return website;
  }

  @Override
  public List<Website> getAll() {
    List<Website> websites = (List<Website>) websiteRepository.findAll();
    return websites;
  }

  @Override
  @Transactional(readOnly = false)
  public Website save(Website website) throws WebsiteServiceException {
    try {
      return (Website) websiteRepository.save(website);
    } catch (Exception e) {
      LOGGER.error("Cannot save website " + website + ": ", e);
      throw new WebsiteServiceException(e.getMessage());
    }
  }

  @Override
  @Transactional(readOnly = false)
  public Website update(Website website) throws WebsiteServiceException {
    try {
      return (Website) websiteRepository.save(website);
    } catch (Exception e) {
      LOGGER.error("Cannot save website " + website + ": ", e);
      throw new WebsiteServiceException(e.getMessage());
    }
  }

//  @Override
//  public Website find(UUID uuid) {
//    return (Website) websiteRepository.find(uuid);
//  }
  public void setWebsiteRepository(WebsiteRepository websiteRepository) {
    this.websiteRepository = websiteRepository;
  }

  @Override
  public List<ContentNode> getRootCategories(Website website) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
