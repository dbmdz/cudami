package de.digitalcollections.cudami.server.business.impl.service.identifiable.web;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.content.ManagedContentService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.identifiable.Identifier;
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
public class WebpageServiceImpl extends IdentifiableServiceImpl<Webpage>
    implements WebpageService, ManagedContentService<Webpage> {

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
  public PageResponse<Webpage> find(PageRequest pageRequest) {
    PageResponse<Webpage> pageResponse = super.find(pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public List<Webpage> find(String searchTerm, int maxResults) {
    List<Webpage> webpages = super.find(searchTerm, maxResults);
    setPublicationStatus(webpages);
    return webpages;
  }

  @Override
  public PageResponse<Webpage> findActiveChildren(UUID uuid, PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    return findChildren(uuid, pageRequest);
  }

  @Override
  public PageResponse<Webpage> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<Webpage> pageResponse =
        super.findByLanguageAndInitial(pageRequest, language, initial);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Webpage> findChildren(UUID uuid, PageRequest pageRequest) {
    PageResponse<Webpage> pageResponse =
        ((NodeRepository<Webpage>) repository).findChildren(uuid, pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Webpage> findRootNodes(PageRequest pageRequest) {
    PageResponse<Webpage> pageResponse =
        ((NodeRepository<Webpage>) repository).findRootNodes(pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Webpage> findRootWebpagesForWebsite(
      UUID websiteUuid, PageRequest pageRequest) {
    PageResponse<Webpage> pageResponse =
        ((WebpageRepository) repository).findRootWebpagesForWebsite(websiteUuid, pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public Webpage getActive(UUID uuid) {
    Filtering filtering = filteringForActive();
    Webpage webpage = ((WebpageRepository) repository).getByUuidAndFiltering(uuid, filtering);
    if (webpage != null) {
      webpage.setChildren(getActiveChildren(uuid));
    }
    setPublicationStatus(webpage);
    return webpage;
  }

  @Override
  public Webpage getActive(UUID uuid, Locale pLocale) {
    Webpage webpage = getActive(uuid);
    webpage = reduceMultilanguageFieldsToGivenLocale(webpage, pLocale);
    setPublicationStatus(webpage);
    return webpage;
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
    List<Webpage> list =
        webpages.stream()
            .peek(w -> w.setChildren(getActiveChildrenTree(w.getUuid())))
            .collect(Collectors.toList());
    setPublicationStatus(list);
    return list;
  }

  @Override
  public List<Webpage> getAllFull() {
    List<Webpage> webpages = super.getAllFull();
    setPublicationStatus(webpages);
    return webpages;
  }

  @Override
  public List<Webpage> getAllReduced() {
    List<Webpage> webpages = super.getAllReduced();
    setPublicationStatus(webpages);
    return webpages;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) {
    return ((NodeRepository<Webpage>) repository).getBreadcrumbNavigation(uuid);
  }

  @Override
  public Webpage getByIdentifier(Identifier identifier) {
    Webpage webpage = super.getByIdentifier(identifier);
    setPublicationStatus(webpage);
    return webpage;
  }

  @Override
  public Webpage getByIdentifier(String namespace, String id) throws ServiceException {
    Webpage webpage = super.getByIdentifier(namespace, id);
    setPublicationStatus(webpage);
    return webpage;
  }

  @Override
  public Webpage getByUuid(UUID uuid) throws ServiceException {
    Webpage webpage = super.getByUuid(uuid);
    setPublicationStatus(webpage);
    return webpage;
  }

  @Override
  public Webpage getByUuidAndLocale(UUID uuid, Locale locale) throws ServiceException {
    Webpage webpage = super.getByUuidAndLocale(uuid, locale);
    setPublicationStatus(webpage);
    return webpage;
  }

  @Override
  public List<Webpage> getChildren(Webpage webpage) {
    List<Webpage> children = ((NodeRepository<Webpage>) repository).getChildren(webpage);
    setPublicationStatus(children);
    return children;
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    List<Webpage> children = ((NodeRepository<Webpage>) repository).getChildren(uuid);
    setPublicationStatus(children);
    return children;
  }

  @Override
  public List<Webpage> getChildrenTree(UUID uuid) {
    List<Webpage> webpages = getChildren(uuid);
    List<Webpage> list =
        webpages.stream()
            .peek(w -> w.setChildren(getChildrenTree(w.getUuid())))
            .collect(Collectors.toList());
    setPublicationStatus(list);
    return list;
  }

  @Override
  public Webpage getParent(UUID webpageUuid) {
    Webpage parent = ((NodeRepository<Webpage>) repository).getParent(webpageUuid);
    setPublicationStatus(parent);
    return parent;
  }

  @Override
  public List<Webpage> getParents(UUID uuid) {
    List<Webpage> parents = ((NodeRepository<Webpage>) repository).getParents(uuid);
    setPublicationStatus(parents);
    return parents;
  }

  @Override
  public List<Locale> getRootNodesLanguages() {
    List<Locale> rootNodesLanguages =
        ((NodeRepository<Webpage>) repository).getRootNodesLanguages();
    return rootNodesLanguages;
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
  public void save(Webpage identifiable) throws ServiceException, ValidationException {
    if (identifiable.getLocalizedUrlAliases() != null
        && !identifiable.getLocalizedUrlAliases().isEmpty()) {
      validate(identifiable.getLocalizedUrlAliases());
    }
    super.save(identifiable);
    setPublicationStatus(identifiable);
  }

  @Override
  public Webpage saveWithParent(UUID childUuid, UUID parentUuid) throws ServiceException {
    try {
      Webpage webpage =
          ((NodeRepository<Webpage>) repository).saveWithParent(childUuid, parentUuid);
      setPublicationStatus(webpage);
      return webpage;
    } catch (Exception e) {
      LOGGER.error("Cannot save webpage " + childUuid + ": ", e);
      throw new ServiceException(e.getMessage());
    }
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid)
      throws ServiceException {
    try {
      if (webpage.getUuid() == null) {
        save(webpage);
      }
      webpage =
          ((WebpageRepository) repository)
              .saveWithParentWebsite(webpage.getUuid(), parentWebsiteUuid);
      setPublicationStatus(webpage);
      return webpage;
    } catch (ServiceException | ValidationException e) {
      LOGGER.error("Cannot save top-level webpage " + webpage + ": ", e);
      throw new ServiceException(e.getMessage());
    }
  }

  @Override
  public void update(Webpage identifiable) throws ServiceException, ValidationException {
    if (identifiable.getLocalizedUrlAliases() != null
        && !identifiable.getLocalizedUrlAliases().isEmpty()) {
      validate(identifiable.getLocalizedUrlAliases());
    }
    super.update(identifiable);
    setPublicationStatus(identifiable);
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
