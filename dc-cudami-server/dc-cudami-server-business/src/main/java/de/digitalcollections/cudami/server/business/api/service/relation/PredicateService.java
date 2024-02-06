package de.digitalcollections.cudami.server.business.api.service.relation;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.relation.Predicate;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Locale;

/** Service for predicates */
public interface PredicateService extends UniqueObjectService<Predicate> {

  boolean deleteByValue(String value) throws ServiceException;

  /**
   * Returns a predicate, if available
   *
   * @param value unique value of predicate, e.g. "is_part_of"
   * @return Predicate or null
   */
  Predicate getByValue(String value) throws ServiceException;

  /**
   * Return list of languages of all predicates
   *
   * @return list of languages
   */
  List<Locale> getLanguages() throws ServiceException;

  /**
   * Update an existing or insert a new predicate.
   *
   * @param predicate the predicate to be updated or inserted
   * @throws ServiceException
   * @throws ValidationException
   */
  void saveOrUpdate(Predicate predicate) throws ValidationException, ServiceException;
}
