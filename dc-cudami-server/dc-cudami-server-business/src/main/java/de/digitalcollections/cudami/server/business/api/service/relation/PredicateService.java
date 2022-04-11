package de.digitalcollections.cudami.server.business.api.service.relation;

import de.digitalcollections.model.relation.Predicate;
import java.util.List;

/** Service for predicates */
public interface PredicateService {

  long count();

  void delete(String value);

  /**
   * @return list of all predicates
   */
  List<Predicate> findAll();

  /**
   * Returns a predicate, if available
   *
   * @param value unique value of predicate, e.g. "is_part_of"
   * @return Predicate or null
   */
  Predicate getByValue(String value);

  /**
   * Saves a predicate. It can either be created or updated
   *
   * @param predicate
   * @return the predicate
   */
  Predicate save(Predicate predicate);
}
