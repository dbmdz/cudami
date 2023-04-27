package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.IdentifierType;

public interface IdentifierTypeRepository extends UniqueObjectRepository<IdentifierType> {

  IdentifierType getByNamespace(String namespace) throws RepositoryException;
}
