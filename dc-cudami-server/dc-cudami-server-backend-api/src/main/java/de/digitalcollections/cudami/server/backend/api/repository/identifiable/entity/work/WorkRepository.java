package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.List;
import java.util.UUID;

/** Repository for Work persistence handling. */
public interface WorkRepository extends EntityRepository<Work> {

  List<Agent> getCreators(UUID workUuid);
}
