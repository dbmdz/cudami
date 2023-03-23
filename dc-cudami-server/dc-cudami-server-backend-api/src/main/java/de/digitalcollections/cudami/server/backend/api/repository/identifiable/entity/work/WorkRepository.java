package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Set;
import java.util.UUID;

/** Repository for Work persistence handling. */
public interface WorkRepository extends EntityRepository<Work> {

  // TODO: remove as work - item is deprecated
  Work getByItemUuid(UUID itemUuid);

  // TODO: replace with find(pagerequest)
  Set<Work> getByPersonUuid(UUID personUuid);

  PageResponse<Work> findEmbeddedWorks(UUID uuid, PageRequest pageRequest);
}
