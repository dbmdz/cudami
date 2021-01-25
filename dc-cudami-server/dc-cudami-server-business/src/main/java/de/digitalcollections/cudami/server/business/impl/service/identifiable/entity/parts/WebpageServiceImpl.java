package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts.WebpageService;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Webpage handling. */
@Service
public class WebpageServiceImpl extends EntityPartServiceImpl<Webpage> implements WebpageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageServiceImpl.class);

  @Autowired
  public WebpageServiceImpl(WebpageRepository repository) {
    super(repository);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<Webpage> collections) {
    return ((NodeRepository<Webpage>) repository).addChildren(parentUuid, collections);
  }

  // TODO: test if webpages work as expected (using now IdentifiableServiceImpl logic)
  //  @Override
  //  public Webpage get(UUID uuid, Locale locale) throws IdentifiableServiceException {
  //    Webpage webpage = super.get(uuid, locale);
  //    if (webpage == null) {
  //      return null;
  //    }
  //
  //    // get the already filtered language to compare with
  //    Locale fLocale = webpage.getLabel().entrySet().iterator().next().getKey();
  //    // filter out not requested translations of fields not already filtered
  //    if (webpage.getText() != null) {
  //      webpage.getText().entrySet().removeIf((Map.Entry entry) ->
  // !entry.getKey().equals(fLocale));
  //    }
  //    return webpage;
  //  }
  @Override
  public Webpage getActive(UUID uuid) {
    Filtering filtering = filteringForActive();
    Webpage webpage = ((WebpageRepository) repository).findOne(uuid, filtering);
    if (webpage != null) {
      webpage.setChildren(getActiveChildren(uuid));
    }
    return webpage;
  }

  @Override
  public Webpage getActive(UUID uuid, Locale pLocale) {
    Webpage webpage = getActive(uuid);
    return reduceMultilanguageFieldsToGivenLocale(webpage, pLocale);
  }

  @Override
  public List<Webpage> getActiveChildren(UUID uuid) {
    Filtering filtering = filteringForActive();
    PageRequest pageRequest = new PageRequestImpl();
    pageRequest.add(filtering);
    return getChildren(uuid, pageRequest).getContent();
  }

  @Override
  public PageResponse<Webpage> getActiveChildren(UUID uuid, PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    return getChildren(uuid, pageRequest);
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) {
    return ((NodeRepository<Webpage>) repository).getBreadcrumbNavigation(uuid);
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
  public Webpage getParent(UUID webpageUuid) {
    return ((NodeRepository<Webpage>) repository).getParent(webpageUuid);
  }

  @Override
  public List<Webpage> getParents(UUID uuid) {
    return ((NodeRepository<Webpage>) repository).getParents(uuid);
  }

  @Override
  public PageResponse<Webpage> getRootNodes(PageRequest pageRequest) {
    return ((NodeRepository<Webpage>) repository).getRootNodes(pageRequest);
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
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    return ((NodeRepository<Webpage>) repository).removeChild(parentUuid, childUuid);
  }

  @Override
  public Webpage saveWithParent(Webpage child, UUID parentUuid)
      throws IdentifiableServiceException {
    try {
      return ((NodeRepository<Webpage>) repository).saveWithParent(child, parentUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save webpage " + child + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
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
  public boolean updateChildrenOrder(UUID parentUuid, List<Webpage> children) {
    return ((NodeRepository<Webpage>) repository).updateChildrenOrder(parentUuid, children);
  }
}
