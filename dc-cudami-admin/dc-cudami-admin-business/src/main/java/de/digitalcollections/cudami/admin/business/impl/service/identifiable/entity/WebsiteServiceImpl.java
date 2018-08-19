package de.digitalcollections.cudami.admin.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.resource.Webpage;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Website handling.
 */
@Service
//@Transactional(readOnly = true)
public class WebsiteServiceImpl extends EntityServiceImpl<Website> implements WebsiteService<Website> {

  @Autowired
  public WebsiteServiceImpl(WebsiteRepository<Website> repository) {
    super(repository);
  }

  @Override
  public List<Webpage> getRootNodes(Website website) {
    return ((WebsiteRepository) repository).getRootPages(website);
  }
}
