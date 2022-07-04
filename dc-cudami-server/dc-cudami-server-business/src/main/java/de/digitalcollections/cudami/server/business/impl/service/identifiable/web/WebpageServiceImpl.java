package de.digitalcollections.cudami.server.business.impl.service.identifiable.web;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
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
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(repository, identifierService, urlAliasService, localeService, cudamiConfig);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<UUID> childrenUuids) {
    return ((NodeRepository<Webpage>) repository).addChildren(parentUuid, childrenUuids);
  }

  @Override
  public PageResponse<Webpage> findActiveChildren(UUID uuid, PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    return findChildren(uuid, pageRequest);
  }

  @Override
  public PageResponse<Webpage> findChildren(UUID uuid, PageRequest pageRequest) {
    return ((NodeRepository<Webpage>) repository).findChildren(uuid, pageRequest);
  }

  @Override
  public PageResponse<Webpage> findRootNodes(PageRequest pageRequest) {
    return ((NodeRepository<Webpage>) repository).findRootNodes(pageRequest);
  }

  @Override
  public PageResponse<Webpage> findRootWebpagesForWebsite(
      UUID websiteUuid, PageRequest pageRequest) {
    return ((WebpageRepository) repository).findRootWebpagesForWebsite(websiteUuid, pageRequest);
  }

  // TODO: test if webpages work as expected (using now IdentifiableServiceImpl logic)
  //  @Override
  //  public Webpage getByIdentifier(UUID uuid, Locale locale) throws IdentifiableServiceException {
  //    Webpage webpage = super.getByIdentifier(uuid, locale);
  //    if (webpage == null) {
  //      return null;
  //    }
  //
  //    // getByIdentifier the already filtered language to compare with
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
    Webpage webpage = ((WebpageRepository) repository).getByUuidAndFiltering(uuid, filtering);
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
    return findChildren(uuid, pageRequest).getContent();
  }

  @Override
  public List<Webpage> getActiveChildrenTree(UUID uuid) {
    List<Webpage> webpages = getActiveChildren(uuid);
    return webpages.stream()
        .peek(w -> w.setChildren(getActiveChildrenTree(w.getUuid())))
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
  public List<Webpage> getChildrenTree(UUID uuid) {
    List<Webpage> webpages = getChildren(uuid);
    return webpages.stream()
        .peek(w -> w.setChildren(getChildrenTree(w.getUuid())))
        .collect(Collectors.toList());
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
  public Webpage save(Webpage identifiable)
      throws IdentifiableServiceException, ValidationException {
    if (identifiable.getLocalizedUrlAliases() != null
        && !identifiable.getLocalizedUrlAliases().isEmpty()) {
      validate(identifiable.getLocalizedUrlAliases());
    }
    return super.save(identifiable);
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
        webpage = save(webpage);
      }
      return ((WebpageRepository) repository)
          .saveWithParentWebsite(webpage.getUuid(), parentWebsiteUuid);
    } catch (IdentifiableServiceException | ValidationException e) {
      LOGGER.error("Cannot save top-level webpage " + webpage + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
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

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Webpage> children) {
    return ((NodeRepository<Webpage>) repository).updateChildrenOrder(parentUuid, children);
  }

  private void validate(LocalizedUrlAliases localizedUrlAliases) throws ValidationException {
    for (UrlAlias urlAlias : localizedUrlAliases.flatten()) {
      if (urlAlias.getWebsite() == null || urlAlias.getWebsite().getUuid() == null) {
        throw new ValidationException("Empty website for " + urlAlias);
      }
    }
  }
}
