package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.model.api.relations.Predicate;
import java.util.List;

/** Repository for predicates handling */
public interface PredicatesRepository {

  /**
   * Returns a predicate, if available
   *
   * @return Predicate or null
   */
  Predicate findOne(String value);

  /**
   * Return all predicates
   *
   * @return List of all predicates
   */
  List<Predicate> findAll();

  /**
   * Save a predicate. Since its field <code>value</code> is its primary key, there's no difference,
   * whether a predicate is created or updated.
   *
   * @param predicate the predicate to be saved
   * @return the saved predicate with update timestamps
   */
  Predicate save(Predicate predicate);
}
