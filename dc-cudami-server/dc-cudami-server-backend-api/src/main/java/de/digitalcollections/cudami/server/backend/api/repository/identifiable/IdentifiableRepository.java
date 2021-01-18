package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import java.util.List;
import java.util.UUID;

public interface IdentifiableRepository<I extends Identifiable> {

  long count();

  default void delete(UUID uuid) {
    delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  void delete(List<UUID> uuids);

  boolean deleteIdentifiers(UUID identifiableUuid);

  PageResponse<I> find(PageRequest pageRequest);

  SearchPageResponse<I> find(SearchPageRequest searchPageRequest);

  default List<I> find(String searchTerm, int maxResults) {
    SearchPageRequestImpl request = new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<I> response = find(request);
    return response.getContent();
  }

  /**
   * @return list of ALL identifiables with FULL data. USE WITH CARE (only for internal workflow, NOT FOR USER INTERACTION!)!!!
   */
  List<I> findAllFull();
  
  /**
   * Returns a list of all identifiables, reduced to their identifiers and last modification date
   *
   * @return partially filled complete list of all identifiables of implementing repository entity
   *     type
   */
  List<I> findAllReduced();

  I findOne(Identifier identifier);

  default I findOne(UUID uuid) {
    return findOne(uuid, null);
  }

  I findOne(UUID uuid, Filtering filtering);

  default I findOneByIdentifier(String namespace, String id) {
    return findOne(new IdentifierImpl(null, namespace, id));
  }

  I save(I identifiable);

  I update(I identifiable);
}
