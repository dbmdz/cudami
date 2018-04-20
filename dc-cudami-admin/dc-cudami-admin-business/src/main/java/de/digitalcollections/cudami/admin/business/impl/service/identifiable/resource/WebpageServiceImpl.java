package de.digitalcollections.cudami.admin.business.impl.service.identifiable.resource;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.WebpageRepository;
import de.digitalcollections.cudami.admin.business.api.service.LocaleService;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.WebpageService;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
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
  public Webpage save(Webpage webpage, UUID websiteUUID, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        webpage = (Webpage) webpageRepository.save(webpage, websiteUUID);
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
