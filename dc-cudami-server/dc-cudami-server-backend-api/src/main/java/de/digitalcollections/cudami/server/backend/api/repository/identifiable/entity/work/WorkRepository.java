package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;

/** Repository for Work persistence handling. */
public interface WorkRepository extends EntityRepository<Work> {

  PageResponse<Work> findByPerson(UUID personUuid);

  PageResponse<Work> findEmbeddedWorks(UUID uuid, PageRequest pageRequest);
}
