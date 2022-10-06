package de.digitalcollections.cudami.server.business.api.service.relation;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.relation.Predicate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for predicates */
public interface PredicateService {

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
   * @return list of all predicates
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
   * @return list of languages
   */
  List<Locale> getLanguages();

  /**
   * Saves a predicate. It can either be created or updated
   *
   * @param predicate
   * @return the predicate
   */
  Predicate save(Predicate predicate);

  /**
   * Update an existing or insert a new predicate.
   *
   * @param predicate the predicate to be updated or inserted
   * @return the updated or inserted predicate
   */
  default Predicate saveOrUpdate(Predicate predicate) {
    UUID uuid = predicate.getUuid();
    if (uuid != null) {
      return update(predicate);
    } else {
      return save(predicate);
    }
  }

  /**
   * Update a predicate.
   *
   * @param predicate the predicate to be updated
   * @return the updated predicate
   */
  Predicate update(Predicate predicate);
}
