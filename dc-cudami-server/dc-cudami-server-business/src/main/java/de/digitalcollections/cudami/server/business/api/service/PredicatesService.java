package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.model.api.relations.Predicate;
import java.util.List;

/** Service for predicates */
public interface PredicatesService {

  /** @return list of all predicates */
  List<Predicate> getPredicates();

  /**
   * Saves a predicate. It can either be created or updated
   *
   * @param predicate
   * @return the predicate
   */
  Predicate save(Predicate predicate);
}
