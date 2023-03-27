package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.List;
import java.util.UUID;

public interface IdentifierRepository extends UniqueObjectRepository<Identifier> {

  default int deleteByIdentifiable(Identifiable identifiable) throws RepositoryException {
    if (identifiable == null) {
      throw new IllegalArgumentException("delete failed: given object must not be null");
    }
    return deleteByIdentifiable(identifiable.getUuid());
  }

  int deleteByIdentifiable(UUID identifiableUuid) throws RepositoryException;

  default List<Identifier> findByIdentifiable(Identifiable identifiable)
      throws RepositoryException {
    if (identifiable == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findByIdentifiable(identifiable.getUuid());
  }

  List<Identifier> findByIdentifiable(UUID identifiableUuid) throws RepositoryException;

  default Identifier getByIdentifier(Identifier identifier) throws RepositoryException {
    if (identifier == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getByUuid(identifier.getUuid());
  }
}
