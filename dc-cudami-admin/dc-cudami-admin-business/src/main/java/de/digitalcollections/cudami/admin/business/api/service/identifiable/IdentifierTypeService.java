package de.digitalcollections.cudami.admin.business.api.service.identifiable;

import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;

public interface IdentifierTypeService {

  IdentifierType create();

  PageResponse<IdentifierType> find(PageRequest pageRequest);

  IdentifierType get(UUID uuid);

  IdentifierType save(IdentifierType identifiable);

  IdentifierType update(IdentifierType identifiable);
}
