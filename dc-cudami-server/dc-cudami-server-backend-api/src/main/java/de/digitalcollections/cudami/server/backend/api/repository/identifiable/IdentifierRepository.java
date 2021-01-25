package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.paging.SearchPageRequestImpl;
import java.util.List;
import java.util.UUID;

public interface IdentifierRepository {

  long count();

  default void delete(UUID uuid) {
    delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  void delete(List<UUID> uuids);
  
  default void deleteByIdentifiable(Identifiable identifiable) {
    deleteByIdentifiable(identifiable.getUuid());
  }
  
  void deleteByIdentifiable(UUID identifiableUuid);

  PageResponse<Identifier> find(PageRequest pageRequest);
  
  SearchPageResponse<Identifier> find(SearchPageRequest searchPageRequest);

  default List<Identifier> find(String searchTerm, int maxResults) {
    SearchPageRequestImpl request = new SearchPageRequestImpl(searchTerm, 0, maxResults, null);
    SearchPageResponse<Identifier> response = find(request);
    return response.getContent();
  }

  List<Identifier> findByIdentifiable(UUID identifiableUuid);

  Identifier findOne(String namespace, String id);

  Identifier save(Identifier identifier);

  Identifier update(Identifier identifier);
}
