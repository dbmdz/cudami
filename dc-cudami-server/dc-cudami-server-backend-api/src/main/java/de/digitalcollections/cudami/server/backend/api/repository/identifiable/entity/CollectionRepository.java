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

/** Repository for Collection persistence handling.
 * @param <C> instance implementing Collection */
public interface CollectionRepository<C extends Collection>
    extends NodeRepository<C>, EntityRepository<C> {

  default boolean addChild(C parent, C child) {
    if (parent == null || child == null) {
      return false;
    }
    return addChildren(parent.getUuid(), Arrays.asList(child));
  }

  default boolean addChildren(C parent, List<C> children) {
    if (parent == null || children == null) {
      return false;
    }
    return addChildren(parent.getUuid(), children);
  }

  boolean addChildren(UUID parentUuid, List<C> collections);

  default boolean addDigitalObject(C collection, DigitalObject digitalObject) {
    if (collection == null || digitalObject == null) {
      return false;
    }
    return addDigitalObjects(collection.getUuid(), Arrays.asList(digitalObject));
  }

  default boolean addDigitalObjects(C collection, List<DigitalObject> digitalObjects) {
    if (collection == null || digitalObjects == null) {
      return false;
    }
    return addDigitalObjects(collection.getUuid(), digitalObjects);
  }

  boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects);

  @Override
  default List<C> getChildren(C collection) {
    if (collection == null) {
      return null;
    }
    return getChildren(collection.getUuid());
  }

  default PageResponse<DigitalObject> getDigitalObjects(
      C collection, PageRequest pageRequest) {
    if (collection == null) {
      return null;
    }
    return getDigitalObjects(collection.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> getDigitalObjects(UUID collectionUuid, PageRequest pageRequest);

  List<C> getParents(UUID uuid);

  List<CorporateBody> getRelatedCorporateBodies(UUID uuid, Filtering filtering);

  PageResponse<C> getTopCollections(PageRequest pageRequest);

  List<Locale> getTopCollectionsLanguages();

  default boolean removeChild(C parent, C child) {
    if (parent == null || child == null) {
      return false;
    }
    return removeChild(parent.getUuid(), child.getUuid());
  }

  boolean removeChild(UUID parentUuid, UUID childUuid);

  default boolean removeDigitalObject(C collection, DigitalObject digitalObject) {
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

  default boolean saveDigitalObjects(C collection, List<DigitalObject> digitalObjects) {
    if (collection == null || digitalObjects == null) {
      return false;
    }
    return saveDigitalObjects(collection.getUuid(), digitalObjects);
  }

  boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects);

  C saveWithParentCollection(C collection, UUID parentUuid);
}
