package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import java.util.List;
import java.util.Set;

public interface UniqueObjectService<U extends UniqueObject>
    extends PagingSortingFilteringService<U> {

  /**
   * @return the count of all unique objects
   */
  long count() throws ServiceException;

  U create() throws ServiceException;

  int delete(Set<U> uniqueObjects) throws ConflictException, ServiceException;

  boolean delete(U uniqueObject) throws ConflictException, ServiceException;

  /**
   * Retrieve one {@code UniqueObject} by given properties in example instance.
   *
   * @param uniqueObject example instance containing unique property
   * @return the found {@code UniqueObject} or {@code null}
   * @throws ServiceException in case of problems
   */
  U getByExample(U uniqueObject) throws ServiceException;

  /**
   * Retrieve one {@code UniqueObject} by given properties in example instance and given filtering.
   *
   * @param uniqueObject example instance containing unique property
   * @param filtering filtering params
   * @return the found {@code UniqueObject} or {@code null}
   * @throws ServiceException in case of problems
   */
  U getByExampleAndFiltering(U uniqueObject, Filtering filtering) throws ServiceException;

  List<U> getRandom(int count) throws ServiceException;

  /**
   * Persist an {@code UniqueObject} (with validation)
   *
   * @param uniqueObject the {@code UniqueObject} (not yet stored)
   * @throws ServiceException in case of an error
   * @throws ValidationException in case of a validation error
   */
  void save(U uniqueObject) throws ValidationException, ServiceException;

  //  // FIXME: bindings?!!! try to get rid of this method...
  //  void save(U uniqueObject, Map<String, Object> bindings) throws ValidationException,
  // ServiceException;

  /**
   * Updates an persisted {@code UniqueObject}
   *
   * @param uniqueObject the {@code UniqueObject} (with set UUID)
   * @throws ServiceException in case of an error
   * @throws ValidationException in case of a validation error
   */
  void update(U uniqueObject) throws ValidationException, ServiceException;

  //  default void update(U uniqueObject) throws ValidationException, ServiceException {
  //    update(uniqueObject, null);
  //  }

  //  // FIXME: bindings?!!! try to get rid of this method...
  //  void update(U uniqueObject, Map<String, Object> bindings) throws ServiceException;
}
