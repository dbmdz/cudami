package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;

public interface IdentifierTypeRepository {

  long count();

  PageResponse<IdentifierType> find(PageRequest pageRequest);

  IdentifierType findByNamespace(String namespace);

  IdentifierType findOne(UUID uuid);

  IdentifierType save(IdentifierType identifier);

  IdentifierType update(IdentifierType identifier);
}
