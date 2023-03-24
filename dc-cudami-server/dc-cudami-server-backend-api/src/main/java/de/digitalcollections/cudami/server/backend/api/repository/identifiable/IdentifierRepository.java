package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface IdentifierRepository {

  long count();

  default boolean delete(Identifier identifier) {
    if (identifier == null) {
      return true;
    }
    return delete(identifier.getUuid());
  }

  default boolean delete(List<Identifier> identifier) throws RepositoryException {
    if (identifier == null || identifier.isEmpty()) {
      return true;
    }
    List<UUID> list = identifier.stream().map(i -> i.getUuid()).collect(Collectors.toList());
    return deleteByUuids(list);
  }

  boolean delete(UUID identifierUuid);

  default int deleteByIdentifiable(Identifiable identifiable) throws RepositoryException {
    if (identifiable == null) {
      return 0;
    }
    return deleteByIdentifiable(identifiable.getUuid());
  }

  int deleteByIdentifiable(UUID identifiableUuid) throws RepositoryException;

  boolean deleteByUuids(List<UUID> uuidList) throws RepositoryException;

  default List<Identifier> findByIdentifiable(Identifiable identifiable)
      throws RepositoryException {
    if (identifiable == null) {
      return null;
    }
    return findByIdentifiable(identifiable.getUuid());
  }

  List<Identifier> findByIdentifiable(UUID identifiableUuid) throws RepositoryException;

  default Identifier getByIdentifier(Identifier identifier) throws RepositoryException {
    if (identifier == null) {
      return null;
    }
    return getByUuid(identifier.getUuid());
  }

  Identifier getByUuid(UUID identifierUuid) throws RepositoryException;

  void save(Identifier identifier) throws RepositoryException;
}
