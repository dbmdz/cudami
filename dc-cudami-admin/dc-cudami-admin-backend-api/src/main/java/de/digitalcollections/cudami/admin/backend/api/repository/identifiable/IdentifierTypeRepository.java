package de.digitalcollections.cudami.admin.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;

public interface IdentifierTypeRepository {

  IdentifierType create();

  PageResponse<IdentifierType> find(PageRequest pageRequest);

  IdentifierType findOne(UUID uuid);

  IdentifierType save(IdentifierType identifierType);

  IdentifierType update(IdentifierType identifierType);
}
