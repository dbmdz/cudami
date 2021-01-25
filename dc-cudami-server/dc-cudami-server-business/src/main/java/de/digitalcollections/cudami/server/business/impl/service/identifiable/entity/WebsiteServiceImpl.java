package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Website handling. */
@Service
public class WebsiteServiceImpl extends EntityServiceImpl<Website> implements WebsiteService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteServiceImpl.class);

  @Autowired
  public WebsiteServiceImpl(WebsiteRepository repository) {
    super(repository);
  }

  @Override
  public List<Locale> getLanguages() {
    return ((WebsiteRepository) repository).getLanguages();
  }

  @Override
  public List<Webpage> getRootPages(UUID uuid) {
    return ((WebsiteRepository) repository).getRootPages(uuid);
  }

  @Override
  public PageResponse<Webpage> getRootPages(UUID uuid, PageRequest pageRequest) {
    return ((WebsiteRepository) repository).getRootPages(uuid, pageRequest);
  }

  @Override
  public boolean updateRootPagesOrder(Website website, List<Webpage> rootPages) {
    return ((WebsiteRepository) repository).updateRootPagesOrder(website, rootPages);
  }

  @Override
  public boolean updateRootPagesOrder(UUID websiteUuid, List<Webpage> rootPages) {
    return ((WebsiteRepository) repository).updateRootPagesOrder(websiteUuid, rootPages);
  }
}
