package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.identifiable.resource.Webpage;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.WebpageService;
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
  private WebpageRepository webpageRepository;

  @Override
  public long count() {
    return webpageRepository.count();
  }

  @Override
  public Webpage create() {
    return (Webpage) webpageRepository.create();
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
  public Webpage save(Webpage webpage) throws IdentifiableServiceException {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  //  @Transactional(readOnly = false)
  public Webpage save(Webpage webpage, UUID websiteUuid) throws IdentifiableServiceException {
    try {
      return (Webpage) webpageRepository.save(webpage, websiteUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public Webpage update(Webpage webpage) throws IdentifiableServiceException {
    try {
      return (Webpage) webpageRepository.update(webpage);
    } catch (Exception e) {
      LOGGER.error("Cannot update webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  public void setRepository(WebpageRepository webpageRepository) {
    this.webpageRepository = webpageRepository;
  }
}
