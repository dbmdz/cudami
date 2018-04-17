package de.digitalcollections.cudami.admin.backend.api.repository.identifiable;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.identifiable.Identifiable;
import java.util.UUID;

public interface IdentifiableRepository<I extends Identifiable> {

  long count();

  I create();

  PageResponse<I> find(PageRequest pageRequest);

  I findOne(UUID uuid);

  I save(I identifiable);

  I update(I identifiable);

}
