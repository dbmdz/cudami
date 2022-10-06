package de.digitalcollections.cudami.server.backend.api.repository.relation;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.relation.Predicate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for predicates handling */
public interface PredicateRepository {

  long count();

  void delete(String value);

  /**
   * Return all predicates paged.
   *
   * @param pageRequest the paging parameters
   * @return Paged list of all predicates
   */
  PageResponse<Predicate> find(PageRequest pageRequest);

  /**
   * Return all predicates
   *
   * @return List of all predicates
   */
  List<Predicate> getAll();

  /**
   * Return predicate with uuid
   *
   * @param uuid the uuid of the predicate
   * @return The found predicate
   */
  Predicate getByUuid(UUID uuid);

  /**
   * Returns a predicate, if available
   *
   * @param value unique value of predicate, e.g. "is_part_of"
   * @return Predicate or null
   */
  Predicate getByValue(String value);

  /**
   * Return list of languages of all predicates
   *
   * @return list of predicates
   */
  List<Locale> getLanguages();

  /**
   * Save a predicate.
   *
   * @param predicate the predicate to be saved
   * @return the saved predicate with update timestamps
   */
  Predicate save(Predicate predicate);

  /**
   * Update a predicate.
   *
   * @param predicate the predicate to be updated
   * @return the updated predicate
   */
  Predicate update(Predicate predicate);
}
