package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
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

  default List<Identifier> find(String searchTerm, int maxResults) {
    PageRequest request = new PageRequest(searchTerm, 0, maxResults, null);
    PageResponse<Identifier> response = find(request);
    return response.getContent();
  }

  List<Identifier> findByIdentifiable(UUID identifiableUuid);

  Identifier getByNamespaceAndId(String namespace, String id);

  Identifier save(Identifier identifier);

  Identifier getByUuid(UUID identifierUuid);

  Identifier update(Identifier identifier);
}
