package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
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
    implements CollectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionServiceImpl.class);

  public CollectionServiceImpl(
      CollectionRepository repository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService,
      HookProperties hookProperties,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        repository,
        identifierRepository,
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
  public PageResponse<Collection> findActive(PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    return find(pageRequest);
  }

  @Override
  public SearchPageResponse<Collection> findActive(SearchPageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    return find(pageRequest);
  }

  @Override
  public SearchPageResponse<Collection> findActiveChildren(
      UUID uuid, SearchPageRequest searchPageRequest) {
    Filtering filtering = filteringForActive();
    searchPageRequest.add(filtering);
    return findChildren(uuid, searchPageRequest);
  }

  @Override
  public SearchPageResponse<Collection> findChildren(
      UUID nodeUuid, SearchPageRequest searchPageRequest) {
    return ((NodeRepository<Collection>) repository).findChildren(nodeUuid, searchPageRequest);
  }

  @Override
  public SearchPageResponse<Collection> findRootNodes(SearchPageRequest searchPageRequest) {
    setDefaultSorting(searchPageRequest);
    return ((NodeRepository<Collection>) repository).findRootNodes(searchPageRequest);
  }

  @Override
  public Collection getActive(UUID uuid) {
    Filtering filtering = filteringForActive();
    Collection collection =
        ((CollectionRepository) repository).getByUuidAndFiltering(uuid, filtering);
    if (collection != null) {
      collection.setChildren(getActiveChildren(uuid));
    }
    return collection;
  }

  @Override
  public Collection getActive(UUID uuid, Locale pLocale) {
    Collection collection = getActive(uuid);
    return reduceMultilanguageFieldsToGivenLocale(collection, pLocale);
  }

  @Override
  public List<Collection> getActiveChildren(UUID uuid) {
    Filtering filtering = filteringForActive();
    PageRequest pageRequest = new PageRequest();
    pageRequest.add(filtering);
    return getChildren(uuid, pageRequest).getContent();
  }

  @Override
  public PageResponse<Collection> getActiveChildren(UUID uuid, PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.add(filtering);
    return getChildren(uuid, pageRequest);
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {
    return ((NodeRepository<Collection>) repository).getBreadcrumbNavigation(nodeUuid);
  }

  @Override
  public List<Collection> getChildren(UUID nodeUuid) {
    return ((NodeRepository<Collection>) repository).getChildren(nodeUuid);
  }

  @Override
  public PageResponse<Collection> getChildren(UUID nodeUuid, PageRequest pageRequest) {
    return ((NodeRepository<Collection>) repository).getChildren(nodeUuid, pageRequest);
  }

  @Override
  public SearchPageResponse<DigitalObject> getDigitalObjects(
      UUID collectionUuid, SearchPageRequest searchPageRequest) {
    return ((CollectionRepository) repository).getDigitalObjects(collectionUuid, searchPageRequest);
  }

  @Override
  public Collection getParent(UUID nodeUuid) {
    return ((NodeRepository<Collection>) repository).getParent(nodeUuid);
  }

  @Override
  public List<Collection> getParents(UUID uuid) {
    return ((CollectionRepository) repository).getParents(uuid);
  }

  @Override
  public List<CorporateBody> getRelatedCorporateBodies(UUID uuid, Filtering filtering) {
    return ((CollectionRepository) repository).getRelatedCorporateBodies(uuid, filtering);
  }

  @Override
  public PageResponse<Collection> getRootNodes(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    return ((NodeRepository<Collection>) repository).getRootNodes(pageRequest);
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
  public boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects) {
    return ((CollectionRepository) repository).saveDigitalObjects(collectionUuid, digitalObjects);
  }

  @Override
  public Collection saveWithParent(UUID childUuid, UUID parentUuid)
      throws IdentifiableServiceException {
    try {
      return ((CollectionRepository) repository).saveWithParent(childUuid, parentUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save collection " + childUuid + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Collection> children) {
    return ((NodeRepository<Collection>) repository).updateChildrenOrder(parentUuid, children);
  }
}
