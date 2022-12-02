package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;

public interface AgentService<A extends Agent> extends EntityService<A> {

  Set<DigitalObject> getDigitalObjects(UUID uuidAgent);

  default Set<Work> getWorks(A agent) {
    if (agent == null) {
      return null;
    }
    return getWorks(agent.getUuid());
  }

  Set<Work> getWorks(UUID uuidAgent);
}
