package de.digitalcollections.cudami.server.business.impl.service.identifiable.web;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.content.ManagedContentService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
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
import de.digitalcollections.model.validation.ValidationException;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Webpage handling. */
// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class WebpageServiceImpl extends IdentifiableServiceImpl<Webpage, WebpageRepository>
    implements WebpageService {

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
  public boolean addChild(Webpage parent, Webpage child) throws ServiceException {
    try {
      return ((NodeRepository<Webpage>) repository).addChild(parent, child);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addChildren(Webpage parent, List<Webpage> children) throws ServiceException {
    try {
      return ((NodeRepository<Webpage>) repository).addChildren(parent, children);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<Webpage> find(PageRequest pageRequest) throws ServiceException {
    PageResponse<Webpage> pageResponse = super.find(pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Webpage> findActiveChildren(Webpage webpage, PageRequest pageRequest)
      throws ServiceException {
    Filtering filtering = ManagedContentService.filteringForActive();
    pageRequest.add(filtering);
    return findChildren(webpage, pageRequest);
  }

  @Override
  public PageResponse<Webpage> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws ServiceException {
    PageResponse<Webpage> pageResponse =
        super.findByLanguageAndInitial(pageRequest, language, initial);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Webpage> findChildren(Webpage webpage, PageRequest pageRequest)
      throws ServiceException {
    PageResponse<Webpage> pageResponse;
    try {
      pageResponse = ((NodeRepository<Webpage>) repository).findChildren(webpage, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Webpage> findRootNodes(PageRequest pageRequest) throws ServiceException {
    PageResponse<Webpage> pageResponse;
    try {
      pageResponse = ((NodeRepository<Webpage>) repository).findRootNodes(pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Webpage> findRootWebpagesForWebsite(Website website, PageRequest pageRequest)
      throws ServiceException {
    PageResponse<Webpage> pageResponse;
    try {
      pageResponse = repository.findRootWebpagesForWebsite(website, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public List<Webpage> getActiveChildren(Webpage webpage) throws ServiceException {
    Filtering filtering = ManagedContentService.filteringForActive();
    PageRequest pageRequest = new PageRequest();
    pageRequest.add(filtering);
    List<Webpage> children = findChildren(webpage, pageRequest).getContent();
    setPublicationStatus(children);
    return children;
  }

  @Override
  public List<Webpage> getActiveChildrenTree(Webpage webpage) throws ServiceException {
    List<Webpage> webpages = getActiveChildren(webpage);
    List<Webpage> list =
        webpages.stream()
            .peek(
                w -> {
                  try {
                    w.setChildren(getActiveChildrenTree(w));
                  } catch (ServiceException e) {
                    LOGGER.error("Can not get active children tree for webpage", e);
                  }
                })
            .collect(Collectors.toList());
    setPublicationStatus(list);
    return list;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(Webpage webpage) throws ServiceException {
    try {
      return ((NodeRepository<Webpage>) repository).getBreadcrumbNavigation(webpage);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Webpage getByExample(Webpage uniqueObject) throws ServiceException {
    Webpage webpage = super.getByExample(uniqueObject);
    if (webpage != null) {
      setPublicationStatus(webpage);
    }
    return webpage;
  }

  @Override
  public Webpage getByExampleAndActive(Webpage example) throws ServiceException {
    Filtering filtering = ManagedContentService.filteringForActive();
    Webpage webpage;
    try {
      webpage = repository.getByExampleAndFiltering(example, filtering);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    if (webpage != null) {
      setPublicationStatus(webpage);
      webpage.setChildren(getActiveChildren(webpage));
    }
    return webpage;
  }

  @Override
  public Webpage getByExampleAndActiveAndLocale(Webpage example, Locale pLocale)
      throws ServiceException {
    Webpage webpage = getByExampleAndActive(example);
    webpage = reduceMultilanguageFieldsToGivenLocale(webpage, pLocale);
    if (webpage != null) {
      setPublicationStatus(webpage);
      webpage.setChildren(getActiveChildren(webpage));
    }
    return webpage;
  }

  @Override
  public Webpage getByIdentifier(Identifier identifier) throws ServiceException {
    Webpage webpage = super.getByIdentifier(identifier);
    if (webpage != null) {
      setPublicationStatus(webpage);
    }
    return webpage;
  }

  @Override
  public Webpage getByExampleAndLocale(Webpage example, Locale locale) throws ServiceException {
    Webpage webpage = super.getByExampleAndLocale(example, locale);
    setPublicationStatus(webpage);
    return webpage;
  }

  @Override
  public List<Webpage> getChildren(Webpage webpage) throws ServiceException {
    List<Webpage> children;
    try {
      children = ((NodeRepository<Webpage>) repository).getChildren(webpage);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(children);
    return children;
  }

  @Override
  public List<Webpage> getChildrenTree(Webpage webpage) throws ServiceException {
    List<Webpage> webpages = getChildren(webpage);
    List<Webpage> list =
        webpages.stream()
            .peek(
                w -> {
                  try {
                    w.setChildren(getChildrenTree(w));
                  } catch (ServiceException e) {
                    LOGGER.error("Can not get children tree for webpage", e);
                  }
                })
            .collect(Collectors.toList());
    setPublicationStatus(list);
    return list;
  }

  @Override
  public Webpage getParent(Webpage webpage) throws ServiceException {
    Webpage parent;
    try {
      parent = ((NodeRepository<Webpage>) repository).getParent(webpage);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(parent);
    return parent;
  }

  @Override
  public List<Webpage> getParents(Webpage webpage) throws ServiceException {
    List<Webpage> parents;
    try {
      parents = ((NodeRepository) repository).getParents(webpage);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(parents);
    return parents;
  }

  @Override
  public List<Webpage> getRandom(int count) throws ServiceException {
    List<Webpage> webpages = super.getRandom(count);
    setPublicationStatus(webpages);
    return webpages;
  }

  @Override
  public List<Locale> getRootNodesLanguages() throws ServiceException {
    try {
      return ((NodeRepository<Webpage>) repository).getRootNodesLanguages();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Website getWebsite(Webpage webpage) throws ServiceException {
    Webpage rootWebpage = webpage;
    Webpage parent = getParent(webpage);
    while (parent != null) {
      rootWebpage = parent;
      parent = getParent(parent);
    }
    // root webpage under a website
    try {
      return repository.getWebsite(rootWebpage);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeChild(Webpage parent, Webpage child) throws ServiceException {
    try {
      return ((NodeRepository<Webpage>) repository).removeChild(parent, child);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
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
  public Webpage saveWithParent(Webpage child, Webpage parent) throws ServiceException {
    try {
      if (child.getUuid() == null) save(child);
      Webpage webpage = repository.saveParentRelation(child, parent);
      setPublicationStatus(webpage);
      return webpage;
    } catch (Exception e) {
      LOGGER.error("Cannot save webpage " + child + ": ", e);
      throw new ServiceException(e.getMessage());
    }
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, Website parentWebsite)
      throws ServiceException {
    try {
      if (webpage.getUuid() == null) {
        save(webpage);
      }
      try {
        webpage = repository.saveWithParentWebsite(webpage, parentWebsite);
      } catch (RepositoryException e) {
        throw new ServiceException("Backend failure", e);
      }
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
  public boolean updateChildrenOrder(Webpage parent, List<Webpage> children)
      throws ServiceException {
    try {
      return ((NodeRepository<Webpage>) repository).updateChildrenOrder(parent, children);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  private void validate(LocalizedUrlAliases localizedUrlAliases) throws ValidationException {
    for (UrlAlias urlAlias : localizedUrlAliases.flatten()) {
      if (urlAlias.getWebsite() == null || urlAlias.getWebsite().getUuid() == null) {
        throw new ValidationException("Empty website for " + urlAlias);
      }
    }
  }
}
