package de.digitalcollections.cudami.server.backend.api.repository.relation;

import de.digitalcollections.model.relation.Predicate;
import java.util.List;

/** Repository for predicates handling */
public interface PredicateRepository {

  long count();

  void delete(String value);

  /**
   * Return all predicates
   *
   * @return List of all predicates
   */
  List<Predicate> getAll();

  /**
   * Returns a predicate, if available
   *
   * @param value unique value of predicate, e.g. "is_part_of"
   * @return Predicate or null
   */
  Predicate getByValue(String value);

  /**
   * Save a predicate. Since its field <code>value</code> is its primary key, there's no difference,
   * whether a predicate is created or updated.
   *
   * @param predicate the predicate to be saved
   * @return the saved predicate with update timestamps
   */
  Predicate save(Predicate predicate);
}
