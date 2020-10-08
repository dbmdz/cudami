package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.CollectionService;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.paging.PageRequestImpl;
import java.time.LocalDate;
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
  public boolean addChild(Collection parent, Collection child) {
    return ((CollectionRepository) repository).addChild(parent, child);
  }

  @Override
  public boolean addChildren(Collection parent, List<Collection> children) {
    return ((CollectionRepository) repository).addChildren(parent, children);
  }

  @Override
  public boolean addDigitalObject(Collection collection, DigitalObject digitalObject) {
    return ((CollectionRepository) repository).addDigitalObject(collection, digitalObject);
  }

  @Override
  public boolean addDigitalObjects(Collection collection, List<DigitalObject> digitalObjects) {
    return ((CollectionRepository) repository).addDigitalObjects(collection, digitalObjects);
  }

  private Filtering filteringForActive() {
    // business logic that defines, what "active" means:
    LocalDate now = LocalDate.now();
    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("publicationStart")
            .lessOrEqual(now)
            .filter("publicationEnd")
            .greaterOrNotSet(now)
            .build();
    return filtering;
  }

  @Override
  public PageResponse<Collection> findActive(PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.setFiltering(filtering);
    return find(pageRequest);
  }

  @Override
  public Collection getActive(UUID uuid) {
    Filtering filtering = filteringForActive();
    return ((CollectionRepository) repository).findOne(uuid, filtering);
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
    pageRequest.setFiltering(filtering);
    return getChildren(uuid, pageRequest).getContent();
  }

  @Override
  public PageResponse<Collection> getActiveChildren(UUID uuid, PageRequest pageRequest) {
    Filtering filtering = filteringForActive();
    pageRequest.setFiltering(filtering);
    return getChildren(uuid, pageRequest);
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID nodeUuid) {
    return ((NodeRepository<Collection>) repository).getBreadcrumbNavigation(nodeUuid);
  }

  @Override
  public List<Collection> getChildren(Collection collection) {
    return ((NodeRepository<Collection>) repository).getChildren(collection);
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
      Collection collection, PageRequest pageRequest) {
    return ((CollectionRepository) repository).getDigitalObjects(collection, pageRequest);
  }

  @Override
  public Collection getParent(Collection node) {
    return getParent(node.getUuid());
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
  public PageResponse<Collection> getTopCollections(PageRequest pageRequest) {
    return ((CollectionRepository) repository).getTopCollections(pageRequest);
  }

  @Override
  public boolean removeChild(Collection parent, Collection child) {
    return ((CollectionRepository) repository).removeChild(parent, child);
  }

  @Override
  public boolean removeDigitalObject(Collection collection, DigitalObject digitalObject) {
    return ((CollectionRepository) repository).removeDigitalObject(collection, digitalObject);
  }

  @Override
  public boolean removeDigitalObjectFromAllCollections(DigitalObject digitalObject) {
    return ((CollectionRepository) repository).removeDigitalObjectFromAllCollections(digitalObject);
  }

  @Override
  public boolean saveDigitalObjects(Collection collection, List<DigitalObject> digitalObjects) {
    return ((CollectionRepository) repository).saveDigitalObjects(collection, digitalObjects);
  }

  @Override
  public Collection saveWithParentCollection(Collection collection, UUID parentUuid)
      throws IdentifiableServiceException {
    try {
      return ((CollectionRepository) repository).saveWithParentCollection(collection, parentUuid);
    } catch (Exception e) {
      LOGGER.error("Cannot save collection " + collection + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
