package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.model.api.identifiable.entity.Website;
import de.digitalcollections.cudami.model.api.identifiable.parts.Text;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
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
 * Service for Website handling.
 */
@Service
//@Transactional(readOnly = true)
public class WebsiteServiceImpl implements WebsiteService<Website> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteServiceImpl.class);

  @Autowired
  private WebsiteRepository websiteRepository;

  @Autowired
  LocaleService localeService;

  @Override
  public long count() {
    return websiteRepository.count();
  }

  @Override
  public Website create() {
    Website website = (Website) websiteRepository.create();
    String defaultLocale = localeService.getDefault().getLanguage();
    Text label = new TextImpl(defaultLocale, "");
    website.setLabel(label);
    Text description = new TextImpl(defaultLocale, "");
    website.setDescription(description);
    return website;
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
  @Transactional(readOnly = false)
  public Website save(Website website, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        website = (Website) websiteRepository.save(website);
      } catch (Exception e) {
        LOGGER.error("Cannot save website " + website + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return website;
  }

  @Override
  @Transactional(readOnly = false)
  public Website update(Website website, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        website = (Website) websiteRepository.update(website);
      } catch (Exception e) {
        LOGGER.error("Cannot update website " + website + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return website;
  }

//  @Override
//  public Website find(UUID uuid) {
//    return (Website) websiteRepository.find(uuid);
//  }
  public void setWebsiteRepository(WebsiteRepository websiteRepository) {
    this.websiteRepository = websiteRepository;
  }

  @Override
  public List<Webpage> getRootNodes(Website website) {
    return websiteRepository.getRootNodes(website);
  }
}
