package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public interface UniqueObjectService<U extends UniqueObject> {

  /**
   * @return the count of all unique objects
   */
  long count() throws ServiceException;

  U create() throws ServiceException;

  int delete(Set<U> uniqueObjects) throws ConflictException, ServiceException;

  boolean delete(U uniqueObject) throws ConflictException, ServiceException;

  PageResponse<U> find(PageRequest pageRequest) throws ServiceException;

  // FIXME: dangerous... but uses paging under the hood... Remove?
  Set<U> getAll() throws ServiceException;

  /**
   * Retrieve one {@code UniqueObject} by given properties in example instance.
   *
   * @param uniqueObject example instance containing unique property
   * @return the found {@code UniqueObject} or null
   * @throws ServiceException in case of problems
   */
  default U getByExample(U uniqueObject) throws ServiceException {
    List<U> uniqueObjects = getByExamples(List.of(uniqueObject));
    return (uniqueObjects == null ? null : uniqueObjects.stream().findFirst().orElse(null));
  }

  /**
   * Retrieve {@code UniqueObject}s by given properties in example instances.
   *
   * @param uniqueObjects example instances containing unique property
   * @return List of found {@code UniqueObject}s
   * @throws ServiceException in case of problems
   */
  List<U> getByExamples(List<U> uniqueObjects) throws ServiceException;

  /**
   * Retrieve {@code UniqueObject}s by given properties in example instances and given filtering.
   *
   * @param uniqueObjects example instances containing unique property
   * @param filtering filtering params
   * @return List of found {@code UniqueObject}s
   * @throws ServiceException in case of problems
   */
  List<U> getByExamplesAndFiltering(List<U> uniqueObjects, Filtering filtering)
      throws ServiceException;

  List<U> getRandom(int count) throws ServiceException;

  /**
   * Persist an {@code UniqueObject} (with validation)
   *
   * @param uniqueObject the {@code UniqueObject} (not yet stored)
   * @throws ServiceException in case of an error
   * @throws ValidationException in case of a validation error
   */
  void save(U uniqueObject) throws ValidationException, ServiceException;

  /**
   * Updates an persisted {@code UniqueObject}
   *
   * @param uniqueObject the {@code UniqueObject} (with set UUID)
   * @throws ServiceException in case of an error
   * @throws ValidationException in case of a validation error
   */
  void update(U uniqueObject) throws ValidationException, ServiceException;

  default U getByExampleAndLocale(U uniqueObject, Locale locale) throws ServiceException {
    throw new ServiceException("Not implemented");
  }
}
