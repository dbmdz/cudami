package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.content.ManagedContentService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service
public class CollectionServiceImpl extends EntityServiceImpl<Collection>
    implements CollectionService, ManagedContentService<Collection> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionServiceImpl.class);

  public CollectionServiceImpl(
      CollectionRepository repository,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierService,
        urlAliasService,
        hookProperties,
        localeService,
        cudamiConfig);
  }

  @Override
  public boolean addChild(Collection parent, Collection child) throws ServiceException {
    try {
      return ((NodeRepository<Collection>) repository).addChild(parent, child);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addChildren(Collection parent, List<Collection> children) throws ServiceException {
    try {
      return ((NodeRepository<Collection>) repository).addChildren(parent, children);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addDigitalObject(Collection collection, DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((CollectionRepository) repository).addDigitalObject(collection, digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean addDigitalObjects(Collection collection, List<DigitalObject> digitalObjects)
      throws ServiceException {
    try {
      return ((CollectionRepository) repository).addDigitalObjects(collection, digitalObjects);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean delete(Collection collection) throws ConflictException, ServiceException {
    long amountChildrenCollections =
        findChildren(collection, PageRequest.builder().pageNumber(0).pageSize(1).build())
            .getTotalElements();
    if (amountChildrenCollections > 0) {
      throw new ConflictException(
          "Collection cannot be deleted, because it has children collections!");
    }

    long amountDigitalObjects =
        findDigitalObjects(collection, PageRequest.builder().pageNumber(0).pageSize(1).build())
            .getTotalElements();
    if (amountDigitalObjects > 0) {
      throw new ConflictException(
          "Collection cannot be deleted, because it has corresponding digital objects!");
    }
    return super.delete(collection);
  }

  @Override
  public PageResponse<Collection> find(PageRequest pageRequest) throws ServiceException {
    PageResponse<Collection> pageResponse;
    try {
      pageResponse = super.find(pageRequest);
    } catch (ServiceException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Collection> findActive(PageRequest pageRequest) throws ServiceException {
    Filtering filtering = ManagedContentService.filteringForActive();
    pageRequest.add(filtering);
    PageResponse<Collection> pageResponse = find(pageRequest);
    return pageResponse;
  }

  @Override
  public PageResponse<Collection> findActiveChildren(Collection collection, PageRequest pageRequest)
      throws ServiceException {
    Filtering filtering = ManagedContentService.filteringForActive();
    pageRequest.add(filtering);
    PageResponse<Collection> pageResponse = findChildren(collection, pageRequest);
    return pageResponse;
  }

  @Override
  public PageResponse<Collection> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws ServiceException {
    PageResponse<Collection> pageResponse =
        super.findByLanguageAndInitial(pageRequest, language, initial);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Collection> findChildren(Collection collection, PageRequest pageRequest)
      throws ServiceException {
    PageResponse<Collection> pageResponse;
    try {
      pageResponse = ((CollectionRepository) repository).findChildren(collection, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjects(
      Collection collection, PageRequest pageRequest) throws ServiceException {
    try {
      return ((CollectionRepository) repository).findDigitalObjects(collection, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<Collection> findRootNodes(PageRequest pageRequest) throws ServiceException {
    setDefaultSorting(pageRequest);
    PageResponse<Collection> pageResponse;
    try {
      pageResponse = ((NodeRepository<Collection>) repository).findRootNodes(pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public List<Collection> getActiveChildren(Collection collection) throws ServiceException {
    Filtering filtering = ManagedContentService.filteringForActive();
    PageRequest pageRequest = new PageRequest();
    pageRequest.add(filtering);
    List<Collection> children = findChildren(collection, pageRequest).getContent();
    setPublicationStatus(children);
    return children;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(Collection collection)
      throws ServiceException {
    try {
      return ((NodeRepository<Collection>) repository).getBreadcrumbNavigation(collection);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Collection getByExample(Collection example) throws ServiceException {
    Collection collection = super.getByExample(example);
    setPublicationStatus(collection);
    return collection;
  }

  @Override
  public Collection getByExampleAndActive(Collection example) throws ServiceException {
    Filtering filtering = ManagedContentService.filteringForActive();
    Collection collection;
    try {
      collection = ((CollectionRepository) repository).getByExampleAndFiltering(example, filtering);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    if (collection != null) {
      setPublicationStatus(collection);
      collection.setChildren(getActiveChildren(collection));
    }
    return collection;
  }

  @Override
  public Collection getByExampleAndActiveAndLocale(Collection example, Locale pLocale)
      throws ServiceException {
    Collection collection = getByExampleAndActive(example);
    collection = reduceMultilanguageFieldsToGivenLocale(collection, pLocale);
    return collection;
  }

  @Override
  public Collection getByExampleAndLocale(Collection example, Locale locale)
      throws ServiceException {
    Collection collection = super.getByExampleAndLocale(example, locale);
    setPublicationStatus(collection);
    return collection;
  }

  @Override
  public Collection getByIdentifier(Identifier identifier) throws ServiceException {
    Collection collection = super.getByIdentifier(identifier);
    setPublicationStatus(collection);
    return collection;
  }

  @Override
  public Collection getByRefId(long refId) throws ServiceException {
    Collection collection = super.getByRefId(refId);
    setPublicationStatus(collection);
    return collection;
  }

  @Override
  public List<Collection> getChildren(Collection collection) throws ServiceException {
    List<Collection> children;
    try {
      children = ((NodeRepository<Collection>) repository).getChildren(collection);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(children);
    return children;
  }

  @Override
  public Collection getParent(Collection collection) throws ServiceException {
    Collection parent;
    try {
      parent = ((NodeRepository<Collection>) repository).getParent(collection);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(parent);
    return parent;
  }

  @Override
  public List<Collection> getParents(Collection collection) throws ServiceException {
    List<Collection> parents;
    try {
      parents = ((NodeRepository) repository).getParents(collection);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    setPublicationStatus(parents);
    return parents;
  }

  @Override
  public List<Collection> getRandom(int count) throws ServiceException {
    List<Collection> collections = super.getRandom(count);
    setPublicationStatus(collections);
    return collections;
  }

  @Override
  public List<Locale> getRootNodesLanguages() throws ServiceException {
    try {
      return ((NodeRepository<Collection>) repository).getRootNodesLanguages();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeChild(Collection parent, Collection child) throws ServiceException {
    try {
      return ((NodeRepository<Collection>) repository).removeChild(parent, child);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeDigitalObject(Collection collection, DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((CollectionRepository) repository).removeDigitalObject(collection, digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean removeDigitalObjectFromAllCollections(DigitalObject digitalObject)
      throws ServiceException {
    try {
      return ((CollectionRepository) repository)
          .removeDigitalObjectFromAllCollections(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void save(Collection entity) throws ServiceException, ValidationException {
    super.save(entity);
    setPublicationStatus(entity);
  }

  @Override
  public Collection saveWithParent(Collection child, Collection parent) throws ServiceException {
    try {
      if (child.getUuid() == null) save(child);
      Collection collection = ((CollectionRepository) repository).saveParentRelation(child, parent);
      setPublicationStatus(collection);
      return collection;
    } catch (Exception e) {
      LOGGER.error("Cannot save collection " + child + ": ", e);
      throw new ServiceException(e.getMessage());
    }
  }

  @Override
  public boolean setDigitalObjects(Collection collection, List<DigitalObject> digitalObjects)
      throws ServiceException {
    try {
      return ((CollectionRepository) repository).setDigitalObjects(collection, digitalObjects);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void update(Collection entity) throws ServiceException, ValidationException {
    super.update(entity);
    setPublicationStatus(entity);
  }

  @Override
  public boolean updateChildrenOrder(Collection parent, List<Collection> children)
      throws ServiceException {
    try {
      return ((NodeRepository<Collection>) repository).updateChildrenOrder(parent, children);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }
}
