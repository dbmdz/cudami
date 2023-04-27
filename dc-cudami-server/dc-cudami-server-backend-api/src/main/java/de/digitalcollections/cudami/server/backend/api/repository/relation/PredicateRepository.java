package de.digitalcollections.cudami.server.backend.api.repository.relation;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.relation.Predicate;
import java.util.List;
import java.util.Locale;

/** Repository for predicates handling */
public interface PredicateRepository extends UniqueObjectRepository<Predicate> {

  boolean deleteByValue(String value) throws RepositoryException;

  /**
   * Returns a predicate, if available
   *
   * @param value unique value of predicate, e.g. "is_part_of"
   * @return Predicate or null
   */
  Predicate getByValue(String value) throws RepositoryException;

  /**
   * Return list of languages of all predicates
   *
   * @return list of predicates
   */
  List<Locale> getLanguages() throws RepositoryException;
}
