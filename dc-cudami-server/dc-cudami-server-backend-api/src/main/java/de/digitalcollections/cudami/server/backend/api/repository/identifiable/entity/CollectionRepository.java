package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Collection persistence handling. */
public interface CollectionRepository
    extends NodeRepository<Collection>, EntityRepository<Collection> {

  default boolean addChild(Collection parent, Collection child) {
    if (parent == null || child == null) {
      return false;
    }
    return addChildren(parent.getUuid(), Arrays.asList(child));
  }

  default boolean addChildren(Collection parent, List<Collection> children) {
    if (parent == null || children == null) {
      return false;
    }
    return addChildren(parent.getUuid(), children);
  }

  boolean addChildren(UUID parentUuid, List<Collection> collections);

  default boolean addDigitalObject(Collection collection, DigitalObject digitalObject) {
    if (collection == null || digitalObject == null) {
      return false;
    }
    return addDigitalObjects(collection.getUuid(), Arrays.asList(digitalObject));
  }

  default boolean addDigitalObjects(Collection collection, List<DigitalObject> digitalObjects) {
    if (collection == null || digitalObjects == null) {
      return false;
    }
    return addDigitalObjects(collection.getUuid(), digitalObjects);
  }

  boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects);

  Collection findOne(UUID uuid, Filtering filtering);

  @Override
  default List<Collection> getChildren(Collection collection) {
    if (collection == null) {
      return null;
    }
    return getChildren(collection.getUuid());
  }

  default PageResponse<DigitalObject> getDigitalObjects(
      Collection collection, PageRequest pageRequest) {
    if (collection == null) {
      return null;
    }
    return getDigitalObjects(collection.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> getDigitalObjects(UUID collectionUuid, PageRequest pageRequest);

  List<Collection> getParents(UUID uuid);

  List<CorporateBody> getRelatedCorporateBodies(UUID uuid, Filtering filtering);

  PageResponse<Collection> getTopCollections(PageRequest pageRequest);

  List<Locale> getTopCollectionsLanguages();

  default boolean removeChild(Collection parent, Collection child) {
    if (parent == null || child == null) {
      return false;
    }
    return removeChild(parent.getUuid(), child.getUuid());
  }

  boolean removeChild(UUID parentUuid, UUID childUuid);

  default boolean removeDigitalObject(Collection collection, DigitalObject digitalObject) {
    if (collection == null || digitalObject == null) {
      return false;
    }
    return removeDigitalObject(collection.getUuid(), digitalObject.getUuid());
  }

  boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid);

  /**
   * Removes a digitalObject from all collections, to which is was connected to
   *
   * @param digitalObject the DigitalObject
   * @return boolean value for success
   */
  boolean removeDigitalObjectFromAllCollections(DigitalObject digitalObject);

  default boolean saveDigitalObjects(Collection collection, List<DigitalObject> digitalObjects) {
    if (collection == null || digitalObjects == null) {
      return false;
    }
    return saveDigitalObjects(collection.getUuid(), digitalObjects);
  }

  boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects);

  Collection saveWithParentCollection(Collection collection, UUID parentUuid);
}
