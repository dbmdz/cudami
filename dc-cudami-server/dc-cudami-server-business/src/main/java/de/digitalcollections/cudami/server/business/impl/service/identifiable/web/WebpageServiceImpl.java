package de.digitalcollections.cudami.server.business.impl.service.identifiable.web;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Webpage handling. */
// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class WebpageServiceImpl extends IdentifiableServiceImpl<Webpage> implements WebpageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageServiceImpl.class);

  @Autowired
  public WebpageServiceImpl(
      WebpageRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService,
      CudamiConfig cudamiConfig) {
    super(repository, identifierRepository, urlAliasService, cudamiConfig);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<UUID> childrenUuids) {
    return ((NodeRepository<Webpage>) repository).addChildren(parentUuid, childrenUuids);
  }

  @Override
  public SearchPageResponse<Webpage> findActiveChildren(
      UUID uuid, SearchPageRequest searchPageRequest) {
    Filtering filtering = filteringForActive();
    searchPageRequest.add(filtering);
    return findChildren(uuid, searchPageRequest);
  }

  @Override
  public SearchPageResponse<Webpage> findChildren(UUID uuid, SearchPageRequest searchPageRequest) {
    return ((NodeRepository<Webpage>) repository).findChildren(uuid, searchPageRequest);
  }

  @Override
  public SearchPageResponse<Webpage> findRootPagesForWebsite(
      UUID websiteUuid, SearchPageRequest pageRequest) {
    return ((WebpageRepository) repository).findRootPagesForWebsite(websiteUuid, pageRequest);
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
    PageRequest pageRequest = new PageRequest();
    pageRequest.add(filtering);
    return getChildren(uuid, pageRequest).getContent();
  }

  @Override
  public List<Webpage> getActiveChildrenTree(UUID uuid) {
    List<Webpage> webpages = getActiveChildren(uuid);
    return webpages.stream()
        .peek(w -> w.setChildren(getActiveChildrenTree(w.getUuid())))
        .collect(Collectors.toList());
  }

  @Override
  public PageResponse<Webpage> getActiveChildren(UUID uuid, PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    return getChildren(uuid, pageRequest);
  }

  @Override
  public List<Webpage> getChildrenTree(UUID uuid) {
    List<Webpage> webpages = getChildren(uuid);
    return webpages.stream()
        .peek(w -> w.setChildren(getChildrenTree(w.getUuid())))
        .collect(Collectors.toList());
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
  public List<Locale> getRootNodesLanguages() {
    return ((NodeRepository<Webpage>) repository).getRootNodesLanguages();
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
  public Webpage saveWithParent(UUID childUuid, UUID parentUuid)
      throws IdentifiableServiceException {
    try {
      return ((NodeRepository<Webpage>) repository).saveWithParent(childUuid, parentUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save webpage " + childUuid + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws IdentifiableServiceException {
    try {
      if (webpage.getUuid() == null) {
        webpage = this.save(webpage);
      }
      return ((WebpageRepository) repository)
          .saveWithParentWebsite(webpage.getUuid(), parentWebsiteUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save top-level webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Webpage> children) {
    return ((NodeRepository<Webpage>) repository).updateChildrenOrder(parentUuid, children);
  }

  @Override
  public SearchPageResponse<Webpage> findRootNodes(SearchPageRequest searchPageRequest) {
    return ((NodeRepository<Webpage>) repository).findRootNodes(searchPageRequest);
  }

  @Override
  public Webpage save(Webpage identifiable)
      throws IdentifiableServiceException, ValidationException {
    if (identifiable.getLocalizedUrlAliases() != null
        && !identifiable.getLocalizedUrlAliases().isEmpty()) {
      validate(identifiable.getLocalizedUrlAliases());
    }
    return super.save(identifiable);
  }

  @Override
  public Webpage update(Webpage identifiable)
      throws IdentifiableServiceException, ValidationException {
    if (identifiable.getLocalizedUrlAliases() != null
        && !identifiable.getLocalizedUrlAliases().isEmpty()) {
      validate(identifiable.getLocalizedUrlAliases());
    }
    return super.update(identifiable);
  }

  private void validate(LocalizedUrlAliases localizedUrlAliases) throws ValidationException {
    for (UrlAlias urlAlias : localizedUrlAliases.flatten()) {
      if (urlAlias.getWebsite() == null || urlAlias.getWebsite().getUuid() == null) {
        throw new ValidationException("Empty website for " + urlAlias);
      }
    }
  }
}
