package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface UniqueObjectRepository<U extends UniqueObject>
    extends PagingSortingFilteringRepository<U> {

  /**
   * Return count of {@code UniqueObject}s.
   *
   * @return the count of {@code UniqueObject}s
   */
  long count() throws RepositoryException;

  /**
   * Create a new instance of {@code UniqueObject}
   *
   * @return new instance of {@code UniqueObject}
   */
  U create() throws RepositoryException;

  /**
   * Delete a list of {@code UniqueObject}s
   *
   * @param uniqueObjects a List of {@code UniqueObject}s
   * @return count of removed datasets
   * @throws RepositoryException
   */
  default int delete(List<U> uniqueObjects) throws RepositoryException {
    if (uniqueObjects == null) {
      throw new IllegalArgumentException("delete failed: given object must not be null");
    }
    if (uniqueObjects.isEmpty()) {
      return 0;
    }
    List<UUID> list = uniqueObjects.stream().map(i -> i.getUuid()).collect(Collectors.toList());
    return deleteByUuids(list);
  }

  default boolean delete(U uniqueObject) throws RepositoryException {
    if (uniqueObject == null) {
      throw new IllegalArgumentException("delete failed: given object must not be null");
    }
    return deleteByUuid(uniqueObject.getUuid());
  }

  /**
   * Delete a single {@code UniqueObject} by its UUID
   *
   * @param uuid the UUID
   * @return true if the {@code UniqueObject} existed and could be deleted or false, if it did not
   *     exist and thus could not be deleted
   * @throws RepositoryException in case of an error
   */
  boolean deleteByUuid(UUID uniqueObjectUuid) throws RepositoryException;

  /**
   * Remove the {@code UniqueObject}s with the provided UUIDs (PK).
   *
   * @param uuidList List of unique object UUIDs to remove
   * @return count of removed datasets
   * @throws RepositoryException
   */
  int deleteByUuids(List<UUID> uuidList) throws RepositoryException;

  /**
   * Retrieve the {@code UniqueObject} with the supplied UUID (PK).
   *
   * @param uuid UUID of unique object
   * @return the found {@code UniqueObject} or {@code null}
   * @throws RepositoryException
   */
  default U getByUuid(UUID uniqueObjectUuid) throws RepositoryException {
    return getByUuidAndFiltering(uniqueObjectUuid, null);
  }

  U getByUuidAndFiltering(UUID uniqueObjectUuid, Filtering filtering) throws RepositoryException;

  /**
   * Save an {@code UniqueObject} object.
   *
   * @param uniqueObject the unique object to save
   * @throws RepositoryException
   */
  default void save(U uniqueObject) throws RepositoryException {
    if (uniqueObject == null) {
      throw new IllegalArgumentException("save failed: given object must not be null");
    }
    save(uniqueObject, null);
  }

  // FIXME: get rid of this (mappings are implementation specific?)
  void save(U uniqueObject, Map<String, Object> bindings) throws RepositoryException;

  /**
   * Update an existing {@code UniqueObject} object.
   *
   * @param uniqueObject the existing object with changed properties
   * @throws RepositoryException
   */
  default void update(U uniqueObject) throws RepositoryException {
    if (uniqueObject == null) {
      throw new IllegalArgumentException("update failed: given object must not be null");
    }
    update(uniqueObject, null);
  }

  // FIXME: get rid of this (mappings are implementation specific?)
  void update(U uniqueObject, Map<String, Object> bindings) throws RepositoryException;
}
