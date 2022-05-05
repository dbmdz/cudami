package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface IdentifierTypeService {

  long count();

  default void delete(UUID uuid) {
    delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  void delete(List<UUID> uuids);

  PageResponse<IdentifierType> find(PageRequest pageRequest);

  IdentifierType getByNamespace(String namespace);

  IdentifierType getByUuid(UUID uuid);

  IdentifierType save(IdentifierType identifierType);

  IdentifierType update(IdentifierType identifierType);
}
