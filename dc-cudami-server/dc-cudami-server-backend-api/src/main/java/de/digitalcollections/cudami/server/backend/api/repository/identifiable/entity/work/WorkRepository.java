package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;

/** Repository for Work persistence handling. */
public interface WorkRepository extends EntityRepository<Work> {

  Set<Work> getWorksForItem(UUID itemUuid);
}
