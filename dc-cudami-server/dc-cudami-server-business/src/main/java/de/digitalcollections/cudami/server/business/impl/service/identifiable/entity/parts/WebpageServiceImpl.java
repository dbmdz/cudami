package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Webpage handling.
 *
 * @param <I> identifiable instance
 */
@Service
//@Transactional(readOnly = true)
public class WebpageServiceImpl<I extends Identifiable> extends IdentifiableServiceImpl<Webpage> implements WebpageService<Webpage, I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageServiceImpl.class);

  @Autowired
  private LocaleService localeService;

  @Autowired
  public WebpageServiceImpl(WebpageRepository<Webpage, I> repository) {
    super(repository);
  }

  @Override
  public Webpage get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    Webpage webpage = repository.findOne(uuid, locale);

    // webpage does not exist in requested language, so try with default locale
    if (webpage == null) {
      webpage = repository.findOne(uuid, localeService.getDefault());
    }

    // webpage does not exist in default locale, so just return first existing language
    if (webpage == null) {
      webpage = repository.findOne(uuid, null);
    }

    return webpage;
  }

  @Override
  public List<Webpage> getChildren(Webpage webpage) {
    return ((NodeRepository) repository).getChildren(webpage);
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    return ((NodeRepository) repository).getChildren(uuid);
  }

  @Override
  //  @Transactional(readOnly = false)
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid) throws IdentifiableServiceException {
    try {
      return ((WebpageRepository) repository).saveWithParentWebsite(webpage, parentWebsiteUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save top-level webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid) throws IdentifiableServiceException {
    try {
      return ((WebpageRepository) repository).saveWithParentWebpage(webpage, parentWebpageUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public List<Identifiable> getIdentifiables(Webpage webpage) {
    return getIdentifiables(webpage.getUuid());
  }

  @Override
  public List<Identifiable> getIdentifiables(UUID identifiableUuid) {
    return ((WebpageRepository) repository).getIdentifiables(identifiableUuid);
  }

  @Override
  public void saveIdentifiables(Webpage webpage, List<Identifiable> identifiables) {
    ((WebpageRepository) repository).saveIdentifiables(webpage, identifiables);
  }
}
