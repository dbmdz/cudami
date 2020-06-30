package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for Webpage handling.
 *
 * @param <E> entity type
 */
@Service
// @Transactional(readOnly = true)
public class WebpageServiceImpl<E extends Entity> extends EntityPartServiceImpl<Webpage, E>
    implements WebpageService<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageServiceImpl.class);

  @Autowired
  public WebpageServiceImpl(WebpageRepository<E> repository) {
    super(repository);
  }

  @Override
  public Webpage get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    Webpage webpage = super.get(uuid, locale);
    if (webpage == null) {
      return null;
    }

    // get the already filtered language to compare with
    Locale fLocale = webpage.getLabel().entrySet().iterator().next().getKey();
    // filter out not requested translations of fields not already filtered
    if (webpage.getText() != null) {
      webpage.getText().entrySet().removeIf((Map.Entry entry) -> !entry.getKey().equals(fLocale));
    }
    return webpage;
  }

  @Override
  public List<Webpage> getChildren(Webpage webpage) {
    return ((NodeRepository<Webpage>) repository).getChildren(webpage);
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    return ((NodeRepository<Webpage>) repository).getChildren(uuid);
  }

  @Override
  public PageResponse<Webpage> getChildren(UUID uuid, PageRequest pageRequest) {
    return ((NodeRepository<Webpage>) repository).getChildren(uuid, pageRequest);
  }

  @Override
  public Webpage getParent(Webpage webpage) {
    return getParent(webpage.getUuid());
  }

  @Override
  public Webpage getParent(UUID webpageUuid) {
    return ((NodeRepository<Webpage>) repository).getParent(webpageUuid);
  }

  @Override
  public Website getWebsite(UUID webpageUuid) {
    UUID rootWebpageUuid = webpageUuid;
    Webpage parent = getParent(webpageUuid);
    while (parent != null) {
      rootWebpageUuid = parent.getUuid();
      parent = getParent(parent);
    }
    // root webpage under a website
    return ((WebpageRepository) repository).getWebsite(rootWebpageUuid);
  }

  @Override
  //  @Transactional(readOnly = false)
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws IdentifiableServiceException {
    try {
      return ((WebpageRepository) repository).saveWithParentWebsite(webpage, parentWebsiteUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save top-level webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid)
      throws IdentifiableServiceException {
    try {
      return ((WebpageRepository) repository).saveWithParentWebpage(webpage, parentWebpageUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) {
    return ((NodeRepository<Webpage>) repository).getBreadcrumbNavigation(uuid);
  }
}
