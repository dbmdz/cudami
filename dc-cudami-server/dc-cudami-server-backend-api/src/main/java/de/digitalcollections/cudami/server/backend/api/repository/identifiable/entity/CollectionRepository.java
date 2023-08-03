package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.NodeRepository;
import de.digitalcollections.model.identifiable.entity.Collection;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** Repository for Collection persistence handling. */
public interface CollectionRepository
    extends NodeRepository<Collection>, EntityRepository<Collection> {

  default boolean addDigitalObject(Collection collection, DigitalObject digitalObject)
      throws RepositoryException {
    if (collection == null || digitalObject == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    return addDigitalObjects(collection.getUuid(), Arrays.asList(digitalObject));
  }

  default boolean addDigitalObjects(Collection collection, List<DigitalObject> digitalObjects)
      throws RepositoryException {
    if (collection == null || digitalObjects == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    return addDigitalObjects(collection.getUuid(), digitalObjects);
  }

  boolean addDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws RepositoryException;

  default PageResponse<DigitalObject> findDigitalObjects(
      Collection collection, PageRequest pageRequest) throws RepositoryException {
    if (collection == null) {
      throw new IllegalArgumentException("find failed: given collection must not be null");
    }
    return findDigitalObjects(collection.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> findDigitalObjects(UUID collectionUuid, PageRequest pageRequest)
      throws RepositoryException;

  default List<CorporateBody> findRelatedCorporateBodies(Collection collection, Filtering filtering)
      throws RepositoryException {
    if (collection == null) {
      throw new IllegalArgumentException("find failed: given collection must not be null");
    }
    return findRelatedCorporateBodies(collection.getUuid(), filtering);
  }

  // FIXME: remove it, just use PageRequest
  List<CorporateBody> findRelatedCorporateBodies(UUID uuid, Filtering filtering)
      throws RepositoryException;

  PageResponse<CorporateBody> findRelatedCorporateBodies(UUID uuid, PageRequest pageRequest)
      throws RepositoryException;

  default boolean removeDigitalObject(Collection collection, DigitalObject digitalObject)
      throws RepositoryException {
    if (collection == null || digitalObject == null) {
      throw new IllegalArgumentException("remove failed: given objects must not be null");
    }
    return removeDigitalObject(collection.getUuid(), digitalObject.getUuid());
  }

  boolean removeDigitalObject(UUID collectionUuid, UUID digitalObjectUuid)
      throws RepositoryException;

  default boolean removeDigitalObjectFromAllCollections(DigitalObject digitalObject)
      throws RepositoryException {
    if (digitalObject == null) {
      throw new IllegalArgumentException("remove failed: given object must not be null");
    }
    return removeDigitalObjectFromAllCollections(digitalObject.getUuid());
  }

  /**
   * Removes a digitalObject from all collections, to which is was connected to
   *
   * @param digitalObjectUuid the UUID of the DigitalObject
   * @return boolean value for success
   */
  boolean removeDigitalObjectFromAllCollections(UUID digitalObjectUuid) throws RepositoryException;

  default boolean setDigitalObjects(Collection collection, List<DigitalObject> digitalObjects)
      throws RepositoryException {
    if (collection == null || digitalObjects == null) {
      throw new IllegalArgumentException("set failed: given objects must not be null");
    }
    return setDigitalObjects(collection.getUuid(), digitalObjects);
  }

  boolean setDigitalObjects(UUID collectionUuid, List<DigitalObject> digitalObjects)
      throws RepositoryException;
}
