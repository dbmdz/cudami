package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import java.util.UUID;

public interface IdentifiableService<I extends Identifiable> {

  long count();

  I create();

  PageResponse<I> find(PageRequest pageRequest);

  I get(UUID uuid);

  I save(I identifiable) throws IdentifiableServiceException;

  I update(I identifiable) throws IdentifiableServiceException;
}
