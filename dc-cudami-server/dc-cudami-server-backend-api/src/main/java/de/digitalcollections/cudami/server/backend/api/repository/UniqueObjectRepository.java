package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
  default int delete(Set<U> uniqueObjects) throws RepositoryException {
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
   * @param uniqueObjectUuid the UUID
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
   * Retrieve one {@code UniqueObject}s by given properties in example instance.
   *
   * @param uniqueObject example instance containing unique property (PK: UUID)
   * @return The found {@code UniqueObject} or null
   * @throws RepositoryException in case of technical problems
   */
  default U getByExample(U uniqueObject) throws RepositoryException {
    if (uniqueObject == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    List<U> uniqueObjects = getByUuidsAndFiltering(List.of(uniqueObject.getUuid()), null);
    return (uniqueObjects == null ? null : uniqueObjects.stream().findFirst().orElse(null));
  }

  /**
   * Retrieve {@code UniqueObject}s by given properties in example instances.
   *
   * @param uniqueObjects example instances containing unique property (PK: UUID)
   * @return List of found {@code UniqueObject}s
   * @throws RepositoryException in case of technical problems
   */
  default List<U> getByExamples(List<U> uniqueObjects) throws RepositoryException {
    if (uniqueObjects == null) {
      throw new IllegalArgumentException("get failed: given objects must not be null");
    }
    return getByUuidsAndFiltering(
        uniqueObjects.stream()
            .map(UniqueObject::getUuid)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()),
        null);
  }

  /**
   * Retrieve a {@code UniqueObject} by given properties in example instance and given filtering.
   *
   * @param uniqueObject example instance containing unique property
   * @param filtering filtering params
   * @return the found {@code UniqueObject} or null
   * @throws RepositoryException in case of problems
   */
  default U getByExampleAndFiltering(U uniqueObject, Filtering filtering)
      throws RepositoryException {
    if (uniqueObject == null || filtering == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    List<U> uniqueObjects = getByUuidsAndFiltering(List.of(uniqueObject.getUuid()), filtering);
    return (uniqueObjects == null ? null : uniqueObjects.stream().findFirst().orElse(null));
  }

  /**
   * Retrieve {@code UniqueObject}s by given properties in example instances and given filtering.
   *
   * @param uniqueObjects example instances containing unique property
   * @param filtering filtering params
   * @return List of found {@code UniqueObject}s
   * @throws RepositoryException in case of problems
   */
  default List<U> getByExamplesAndFiltering(List<U> uniqueObjects, Filtering filtering)
      throws RepositoryException {
    if (uniqueObjects == null || filtering == null) {
      throw new IllegalArgumentException("get failed: given objects must not be null");
    }
    return getByUuidsAndFiltering(
        uniqueObjects.stream()
            .map(UniqueObject::getUuid)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()),
        filtering);
  }

  /**
   * Retrieves the {@code UniqueObject} with the supplied UUIDs (PK).
   *
   * @param uniqueObjectUuid UUID of unique object
   * @return the found {@code UniqueObject} or null
   * @throws RepositoryException
   */
  default U getByUuid(UUID uniqueObjectUuid) throws RepositoryException {
    List<U> uniqueObjects = getByUuidsAndFiltering(List.of(uniqueObjectUuid), null);
    return (uniqueObjects == null ? null : uniqueObjects.stream().findFirst().orElse(null));
  }

  /**
   * Retrieves the {@code UniqueObject}s with the supplied UUIDs (PK).
   *
   * @param uniqueObjectUuids UUIDs of unique objects
   * @return list of found {@code UniqueObject}s
   * @throws RepositoryException
   */
  default List<U> getByUuids(List<UUID> uniqueObjectUuids) throws RepositoryException {
    return getByUuidsAndFiltering(uniqueObjectUuids, null);
  }

  List<U> getByUuidsAndFiltering(List<UUID> uniqueObjectUuids, Filtering filtering)
      throws RepositoryException;

  default U getByUuidAndFiltering(UUID uniqueObjectUuid, Filtering filtering)
      throws RepositoryException {
    if (uniqueObjectUuid == null || filtering == null) {
      throw new IllegalArgumentException(
          "get failed: given uuid and/or filtering must not be null");
    }
    List<U> uniqueObjects = getByUuidsAndFiltering(List.of(uniqueObjectUuid), filtering);
    return uniqueObjects.stream().findFirst().orElse(null);
  }

  List<U> getRandom(int count) throws RepositoryException;

  /**
   * Save an {@code UniqueObject} object.
   *
   * @param uniqueObject the unique object to save
   * @throws RepositoryException
   * @throws ValidationException
   */
  default void save(U uniqueObject) throws RepositoryException, ValidationException {
    if (uniqueObject == null) {
      throw new IllegalArgumentException("save failed: given object must not be null");
    }
    save(uniqueObject, null);
  }

  void save(U uniqueObject, Map<String, Object> bindings)
      throws RepositoryException, ValidationException;

  default void saveOrUpdate(U uniqueObject) throws RepositoryException, ValidationException {
    UUID uuid = uniqueObject.getUuid();
    if (uuid != null) {
      update(uniqueObject);
    } else {
      save(uniqueObject);
    }
  }

  /**
   * Update an existing {@code UniqueObject} object.
   *
   * @param uniqueObject the existing object with changed properties
   * @throws RepositoryException
   * @throws ValidationException
   */
  default void update(U uniqueObject) throws RepositoryException, ValidationException {
    if (uniqueObject == null) {
      throw new IllegalArgumentException("update failed: given object must not be null");
    }
    update(uniqueObject, null);
  }

  // FIXME: get rid of this (mappings are implementation specific?)
  void update(U uniqueObject, Map<String, Object> bindings)
      throws RepositoryException, ValidationException;
}
