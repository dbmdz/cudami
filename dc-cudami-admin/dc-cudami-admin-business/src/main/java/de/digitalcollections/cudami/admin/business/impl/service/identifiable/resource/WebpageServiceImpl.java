package de.digitalcollections.cudami.admin.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.WebpageRepository;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.WebpageService;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

/**
 * Service for Webpage handling.
 */
@Service
//@Transactional(readOnly = true)
public class WebpageServiceImpl implements WebpageService<Webpage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageServiceImpl.class);

  @Autowired
  private WebpageRepository webpageRepository;

  @Autowired
  LocaleService localeService;

  @Override
  public long count() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Webpage create() {
    Webpage webpage = (Webpage) webpageRepository.create();
    return webpage;
  }

  @Override
  public PageResponse<Webpage> find(PageRequest pageRequest) {
    return webpageRepository.find(pageRequest);
  }

  @Override
  public Webpage get(UUID uuid) {
    return (Webpage) webpageRepository.findOne(uuid);
  }

  @Override
  public Webpage save(Webpage webpage, Errors results) throws IdentifiableServiceException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUUID, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        webpage = (Webpage) webpageRepository.saveWithParentWebsite(webpage, parentWebsiteUUID);
      } catch (Exception e) {
        LOGGER.error("Cannot save top-level webpage " + webpage + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return webpage;
  }

  @Override
  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUUID, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        webpage = (Webpage) webpageRepository.saveWithParentWebpage(webpage, parentWebpageUUID);
      } catch (Exception e) {
        LOGGER.error("Cannot save webpage " + webpage + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return webpage;
  }

  public void setWebpageRepository(WebpageRepository webpageRepository) {
    this.webpageRepository = webpageRepository;
  }

  @Override
  public Webpage update(Webpage webpage, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        webpage = (Webpage) webpageRepository.update(webpage);
      } catch (Exception e) {
        LOGGER.error("Cannot update webpage " + webpage + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    return webpage;
  }
}
