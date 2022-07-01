package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.List;
import java.util.UUID;

public interface IdentifierRepository {

  long count();

  default void delete(UUID uuid) throws RepositoryException {
    delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  void delete(List<UUID> uuids) throws RepositoryException;

  default int deleteByIdentifiable(Identifiable identifiable) throws RepositoryException {
    return deleteByIdentifiable(identifiable.getUuid());
  }

  int deleteByIdentifiable(UUID identifiableUuid) throws RepositoryException;

  List<Identifier> findByIdentifiable(UUID identifiableUuid) throws RepositoryException;

  Identifier getByUuid(UUID identifierUuid) throws RepositoryException;

  Identifier save(Identifier identifier) throws RepositoryException;
}
