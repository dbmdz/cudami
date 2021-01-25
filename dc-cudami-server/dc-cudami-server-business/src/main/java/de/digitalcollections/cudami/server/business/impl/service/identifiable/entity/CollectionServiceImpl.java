package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CollectionServiceImpl extends EntityServiceImpl<Collection>
    implements CollectionService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionServiceImpl.class);

  @Autowired
  public CollectionServiceImpl(CollectionRepository repository) {
    super(repository);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<Collection> children) {
    return ((NodeRepository<Collection>) repository).addChildren(parentUuid, children);
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
  public Collection getActive(UUID uuid) {
    Filtering filtering = filteringForActive();
    Collection collection = ((CollectionRepository) repository).findOne(uuid, filtering);
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
    PageRequest pageRequest = new PageRequestImpl();
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
  public List<Collection> getChildren(UUID uuid) {
    return ((NodeRepository<Collection>) repository).getChildren(uuid);
  }

  @Override
  public PageResponse<Collection> getChildren(UUID uuid, PageRequest pageRequest) {
    return ((NodeRepository<Collection>) repository).getChildren(uuid, pageRequest);
  }

  @Override
  public PageResponse<DigitalObject> getDigitalObjects(
      UUID collectionUuid, PageRequest pageRequest) {
    return ((CollectionRepository) repository).getDigitalObjects(collectionUuid, pageRequest);
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
    return ((NodeRepository<Collection>) repository).getRootNodes(pageRequest);
  }

  @Override
  public List<Locale> getTopCollectionsLanguages() {
    return ((CollectionRepository) repository).getTopCollectionsLanguages();
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
  public Collection saveWithParent(Collection child, UUID parentUuid)
      throws IdentifiableServiceException {
    try {
      return ((CollectionRepository) repository).saveWithParent(child, parentUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save collection " + child + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Collection> children) {
    return ((NodeRepository<Collection>) repository).updateChildrenOrder(parentUuid, children);
  }
}
