package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.WebpageService;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Webpage handling.
 */
@Service
//@Transactional(readOnly = true)
public class WebpageServiceImpl implements WebpageService<Webpage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageServiceImpl.class);

  @Autowired
  private WebpageRepository<Webpage> webpageRepository;

  @Autowired
  private LocaleService localeService;

  @Override
  public long count() {
    return webpageRepository.count();
  }

  @Override
  public Webpage create() {
    return webpageRepository.create();
  }

  @Override
  public PageResponse<Webpage> find(PageRequest pageRequest) {
    return webpageRepository.find(pageRequest);
  }

  @Override
  public Webpage get(UUID uuid) {
    return webpageRepository.findOne(uuid);
  }

  @Override
  public Webpage get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    Webpage webpage = webpageRepository.findOne(uuid, locale);

    // webpage does not exist in requested language, so try with default locale
    if (webpage == null) {
      webpage = webpageRepository.findOne(uuid, localeService.getDefault());
    }

    // webpage does not exist in default locale, so just return first existing language
    if (webpage == null) {
      webpage = webpageRepository.findOne(uuid, null);
    }

    return webpage;
  }

  @Override
  public List<Webpage> getSubPages(Webpage webpage) {
    return webpageRepository.getSubPages(webpage);
  }

  @Override
  public Webpage save(Webpage webpage) throws IdentifiableServiceException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  //  @Transactional(readOnly = false)
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid) throws IdentifiableServiceException {
    try {
      return webpageRepository.saveWithParentWebsite(webpage, parentWebsiteUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save top-level webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid) throws IdentifiableServiceException {
    try {
      return webpageRepository.saveWithParentWebpage(webpage, parentWebpageUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public Webpage update(Webpage webpage) throws IdentifiableServiceException {
    try {
      return webpageRepository.update(webpage);
    } catch (Exception e) {
      LOGGER.error("Cannot update webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  public void setRepository(WebpageRepository webpageRepository) {
    this.webpageRepository = webpageRepository;
  }
}
