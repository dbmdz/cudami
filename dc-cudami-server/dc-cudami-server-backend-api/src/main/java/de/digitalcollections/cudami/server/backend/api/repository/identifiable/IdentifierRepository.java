package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

  default List<Identifier> getByIdentifiers(List<Identifier> identifiers)
      throws RepositoryException {
    if (identifiers == null) {
      throw new IllegalArgumentException("get failed: given objects must not be null");
    }
    return getByUuids(
        identifiers.stream()
            .map(Identifier::getUuid)
            .filter(Objects::nonNull)
            .collect(Collectors.toList()));
  }

  @Override
  default void save(Identifier uniqueObject) throws RepositoryException {
    throw new UnsupportedOperationException(
        "saving without related Identifiable not supported - use saveForIdentifiable instead.");
  }

  Set<Identifier> saveForIdentifiable(Identifiable identifiable, Set<Identifier> identifiers)
      throws RepositoryException, ValidationException;
}
