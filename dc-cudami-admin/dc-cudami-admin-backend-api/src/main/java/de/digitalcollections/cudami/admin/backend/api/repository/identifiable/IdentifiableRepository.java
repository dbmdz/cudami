package de.digitalcollections.cudami.admin.backend.api.repository.identifiable;

import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;

public interface IdentifiableRepository<I extends Identifiable> {

  long count();

  I create();

  PageResponse<I> find(PageRequest pageRequest);

  List<I> find(String searchTerm, int maxResults);

  I findOne(UUID uuid);

  I save(I identifiable);

  I update(I identifiable);
}
