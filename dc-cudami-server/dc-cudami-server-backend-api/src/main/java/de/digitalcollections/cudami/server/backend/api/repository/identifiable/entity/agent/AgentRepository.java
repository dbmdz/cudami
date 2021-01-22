package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;

public interface AgentRepository extends EntityRepository<Agent> {

  Set<DigitalObject> getDigitalObjects(UUID uuidAgent);

  default Set<Work> getWorks(Agent agent) {
    if (agent == null) {
      return null;
    }
    return getWorks(agent.getUuid());
  }

  Set<Work> getWorks(UUID uuidAgent);
}
