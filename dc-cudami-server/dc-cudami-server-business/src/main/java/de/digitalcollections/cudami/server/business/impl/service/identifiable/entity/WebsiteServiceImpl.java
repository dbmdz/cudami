package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
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
public class WebsiteServiceImpl extends EntityServiceImpl<Website> implements WebsiteService<Website> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteServiceImpl.class);

  @Autowired
  public WebsiteServiceImpl(WebsiteRepository<Website> repository) {
    super(repository);
  }

  @Override
  public List<Webpage> getRootPages(Website website) {
    return ((WebsiteRepository) repository).getRootPages(website);
  }

  @Override
  public List<Webpage> getRootPages(UUID uuid) {
    return ((WebsiteRepository) repository).getRootPages(uuid);
  }
}
