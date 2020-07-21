package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Repository for Collection persistence handling. */
public interface CollectionRepository
    extends NodeRepository<Collection>, EntityRepository<Collection> {

  @Override
  default List<Collection> getChildren(Collection collection) {
    if (collection == null) {
      return null;
    }
    return getChildren(collection.getUuid());
  }

  Collection saveWithParentCollection(Collection collection, UUID parentUuid);

  PageResponse<Collection> getTopCollections(PageRequest pageRequest);

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

  default PageResponse<DigitalObject> getDigitalObjects(
      Collection collection, PageRequest pageRequest) {
    if (collection == null) {
      return null;
    }
    return getDigitalObjects(collection.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> getDigitalObjects(UUID collectionUuid, PageRequest pageRequest);

  default boolean saveDigitalObjects(Collection collection, List<DigitalObject> digitalObjects) {
    if (collection == null || digitalObjects == null) {
      return false;
    }
    return saveDigitalObjects(collection.getUuid(), digitalObjects);
  }

  boolean saveDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects);
}
