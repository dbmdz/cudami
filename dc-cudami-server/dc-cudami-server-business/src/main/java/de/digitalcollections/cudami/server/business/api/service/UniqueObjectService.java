package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import java.util.List;
import java.util.Map;

public interface UniqueObjectService<U extends UniqueObject>
    extends PagingSortingFilteringService<U> {

  /**
   * @return the count of all unique objects
   */
  long count() throws ServiceException;

  U create() throws ServiceException;

  int delete(List<U> uniqueObjects) throws ConflictException, ServiceException;

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

  /**
   * Persist an {@code UniqueObject} (with validation)
   *
   * @param uniqueObject the {@code UniqueObject} (with yet empty UUID)
   * @throws ServiceException in case of an error
   * @throws ValidationException in case of a validation error
   */
  default U save(U uniqueObject) throws ValidationException, ServiceException {
    if (uniqueObject == null) {
      throw new ServiceException("null object can not be saved");
    }
    return save(uniqueObject, false);
  }

  /**
   * Persist an {@code UniqueObject} (with optional validation)
   *
   * @param uniqueObject the {@code UniqueObject} (with yet empty UUID)
   * @throws ServiceException in case of an error
   * @throws ValidationException in case of a validation error
   */
  // FIXME: UseCase?!!! Never skip validation! get rid of this method...
  U save(U uniqueObject, boolean skipValidation) throws ValidationException, ServiceException;

  // FIXME: bindings?!!! try to get rid of this method...
  U save(U uniqueObject, Map<String, Object> bindings) throws ValidationException, ServiceException;

  /**
   * Updates an persisted {@code UniqueObject}
   *
   * @param uniqueObject the {@code UniqueObject} (with set UUID)
   * @throws ServiceException in case of an error
   * @throws ValidationException in case of a validation error
   */
  default U update(U uniqueObject) throws ValidationException, ServiceException {
    return update(uniqueObject, null);
  }

  // FIXME: bindings?!!! try to get rid of this method...
  U update(U uniqueObject, Map<String, Object> bindings) throws ServiceException;
}
