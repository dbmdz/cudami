package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.content.ManagedContentService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
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
  public boolean addChildren(UUID parentUuid, List<UUID> childrenUuids) {
    return ((NodeRepository<Collection>) repository).addChildren(parentUuid, childrenUuids);
  }

  @Override
  public boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects) {
    return ((CollectionRepository) repository).addDigitalObjects(collectionUuid, digitalObjects);
  }

  @Override
  public boolean delete(UUID uuid) throws ConflictException, ServiceException {
    long amountChildrenCollections =
        findChildren(uuid, PageRequest.builder().pageNumber(0).pageSize(1).build())
            .getTotalElements();
    if (amountChildrenCollections > 0) {
      throw new ConflictException(
          "Collection cannot be deleted, because it has children collections!");
    }

    long amountDigitalObjects =
        findDigitalObjects(uuid, PageRequest.builder().pageNumber(0).pageSize(1).build())
            .getTotalElements();
    if (amountDigitalObjects > 0) {
      throw new ConflictException(
          "Collection cannot be deleted, because it has corresponding digital objects!");
    }
    return super.deleteByUuid(uuid);
  }

  @Override
  public PageResponse<Collection> find(PageRequest pageRequest) {
    PageResponse<Collection> pageResponse = super.find(pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Collection> findActive(PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    PageResponse<Collection> pageResponse = find(pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Collection> findActiveChildren(UUID uuid, PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    PageResponse<Collection> pageResponse = findChildren(uuid, pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Collection> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<Collection> pageResponse =
        super.findByLanguageAndInitial(pageRequest, language, initial);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<Collection> findChildren(UUID nodeUuid, PageRequest pageRequest) {
    PageResponse<Collection> pageResponse =
        ((NodeRepository<Collection>) repository).findChildren(nodeUuid, pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjects(
      UUID collectionUuid, PageRequest pageRequest) {
    return ((CollectionRepository) repository).findDigitalObjects(collectionUuid, pageRequest);
  }

  @Override
  public List<CorporateBody> findRelatedCorporateBodies(UUID uuid, Filtering filtering) {
    return ((CollectionRepository) repository).findRelatedCorporateBodies(uuid, filtering);
  }

  @Override
  public PageResponse<Collection> findRootNodes(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    PageResponse<Collection> pageResponse =
        ((NodeRepository<Collection>) repository).findRootNodes(pageRequest);
    setPublicationStatus(pageResponse.getContent());
    return pageResponse;
  }

  @Override
  public Collection getActive(UUID uuid) {
    Filtering filtering = filteringForActive();
    Collection collection =
        ((CollectionRepository) repository).getByUuidAndFiltering(uuid, filtering);
    if (collection != null) {
      collection.setChildren(getActiveChildren(uuid));
      setPublicationStatus(collection);
    }
    return collection;
  }

  @Override
  public Collection getActive(UUID uuid, Locale pLocale) {
    Collection collection = getActive(uuid);
    collection = reduceMultilanguageFieldsToGivenLocale(collection, pLocale);
    setPublicationStatus(collection);
    return collection;
  }

  @Override
  public List<Collection> getActiveChildren(UUID uuid) {
    Filtering filtering = filteringForActive();
    PageRequest pageRequest = new PageRequest();
    pageRequest.add(filtering);
    List<Collection> children = findChildren(uuid, pageRequest).getContent();
    setPublicationStatus(children);
    return children;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {
    return ((NodeRepository<Collection>) repository).getBreadcrumbNavigation(nodeUuid);
  }

  @Override
  public Collection getByIdentifier(Identifier identifier) {
    Collection collection = super.getByIdentifier(identifier);
    setPublicationStatus(collection);
    return collection;
  }

  @Override
  public Collection getByRefId(long refId) {
    Collection collection = super.getByRefId(refId);
    setPublicationStatus(collection);
    return collection;
  }

  @Override
  public Collection getByUuid(UUID uuid) throws ServiceException {
    Collection collection = super.getByUuid(uuid);
    setPublicationStatus(collection);
    return collection;
  }

  @Override
  public Collection getByUuidAndLocale(UUID uuid, Locale locale) throws ServiceException {
    Collection collection = super.getByUuidAndLocale(uuid, locale);
    setPublicationStatus(collection);
    return collection;
  }

  @Override
  public List<Collection> getChildren(UUID nodeUuid) {
    List<Collection> children = ((NodeRepository<Collection>) repository).getChildren(nodeUuid);
    setPublicationStatus(children);
    return children;
  }

  @Override
  public Collection getParent(UUID nodeUuid) {
    Collection parent = ((NodeRepository<Collection>) repository).getParent(nodeUuid);
    setPublicationStatus(parent);
    return parent;
  }

  @Override
  public List<Collection> getParents(UUID uuid) {
    List<Collection> parents = ((CollectionRepository) repository).getParents(uuid);
    setPublicationStatus(parents);
    return parents;
  }

  @Override
  public List<Collection> getRandom(int count) {
    List<Collection> collections = super.getRandom(count);
    setPublicationStatus(collections);
    return collections;
  }

  @Override
  public List<Locale> getRootNodesLanguages() {
    return ((NodeRepository<Collection>) repository).getRootNodesLanguages();
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    return ((NodeRepository<Collection>) repository).removeChild(parentUuid, childUuid);
  }

  @Override
  public boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid) {
    return ((CollectionRepository) repository)
        .removeDigitalObject(collectionUuid, digitalObjectUuid);
  }

  @Override
  public boolean removeDigitalObjectFromAllCollections(DigitalObject digitalObject) {
    return ((CollectionRepository) repository).removeDigitalObjectFromAllCollections(digitalObject);
  }

  @Override
  public void save(Collection entity) throws ServiceException, ValidationException {
    super.save(entity);
    setPublicationStatus(entity);
  }

  @Override
  public Collection saveWithParent(UUID childUuid, UUID parentUuid) throws ServiceException {
    try {
      Collection collection =
          ((CollectionRepository) repository).saveWithParent(childUuid, parentUuid);
      setPublicationStatus(collection);
      return collection;
    } catch (Exception e) {
      LOGGER.error("Cannot save collection " + childUuid + ": ", e);
      throw new ServiceException(e.getMessage());
    }
  }

  @Override
  public boolean setDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects) {
    return ((CollectionRepository) repository).setDigitalObjects(collectionUuid, digitalObjects);
  }

  @Override
  public void update(Collection entity) throws ServiceException, ValidationException {
    super.update(entity);
    setPublicationStatus(entity);
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Collection> children) {
    return ((NodeRepository<Collection>) repository).updateChildrenOrder(parentUuid, children);
  }
}
