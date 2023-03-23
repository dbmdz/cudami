package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface IdentifierTypeRepository extends UniqueObjectRepository<IdentifierType> {

  long count();

  default void delete(UUID uuid) {
    delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  void delete(List<UUID> uuids);

  PageResponse<IdentifierType> find(PageRequest pageRequest);

  IdentifierType getByNamespace(String namespace);

  IdentifierType save(IdentifierType identifier) throws RepositoryException;

  IdentifierType update(IdentifierType identifier) throws RepositoryException;
}
