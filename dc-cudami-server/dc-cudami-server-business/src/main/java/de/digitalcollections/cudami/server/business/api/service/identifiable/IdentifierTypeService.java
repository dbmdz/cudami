package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;

public interface IdentifierTypeService {

  long count();

  PageResponse find(PageRequest pageRequest);

  IdentifierType get(UUID uuid);

  IdentifierType save(IdentifierType identifierType);

  IdentifierType update(IdentifierType identifierType);
}
