package de.digitalcollections.cudami.admin.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.WebpageRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.WebpageService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import java.util.List;
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
public class WebpageServiceImpl extends ResourceServiceImpl<Webpage> implements WebpageService<Webpage> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageServiceImpl.class);

  @Autowired
  public WebpageServiceImpl(WebpageRepository<Webpage> repository) {
    super(repository);
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUUID, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        webpage = (Webpage) ((WebpageRepository) repository).saveWithParentWebsite(webpage, parentWebsiteUUID);
      } catch (Exception e) {
        LOGGER.error("Cannot save top-level webpage " + webpage + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    // FIXME: what if results has errors? throw exception?
    return webpage;
  }

  @Override
  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUUID, Errors results) throws IdentifiableServiceException {
    if (!results.hasErrors()) {
      try {
        webpage = (Webpage) ((WebpageRepository) repository).saveWithParentWebpage(webpage, parentWebpageUUID);
      } catch (Exception e) {
        LOGGER.error("Cannot save webpage " + webpage + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    // FIXME: what if results has errors? throw exception?
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
  public List<Identifiable> getIdentifiables(Webpage webpage) {
    return ((WebpageRepository) repository).getIdentifiables(webpage);
  }
}
