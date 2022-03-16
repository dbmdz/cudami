package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface IdentifierTypeRepository {

  long count();

  default void delete(UUID uuid) {
    delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  void delete(List<UUID> uuids);

  PageResponse<IdentifierType> find(PageRequest pageRequest);

  IdentifierType getByUuid(UUID uuid);

  IdentifierType getByNamespace(String namespace);

  IdentifierType save(IdentifierType identifier);

  IdentifierType update(IdentifierType identifier);
}
