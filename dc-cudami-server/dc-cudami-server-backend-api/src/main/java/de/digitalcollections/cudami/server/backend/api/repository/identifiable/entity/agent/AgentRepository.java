package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Set;
import java.util.UUID;

public interface AgentRepository<A extends Agent> extends EntityRepository<A> {

  default PageResponse<DigitalObject> findDigitalObjects(A agent, PageRequest pageRequest)
      throws RepositoryException {
    if (agent == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findDigitalObjects(agent.getUuid(), pageRequest);
  }

  PageResponse<DigitalObject> findDigitalObjects(UUID agentUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<Work> findWorks(A agent, PageRequest pageRequest)
      throws RepositoryException {
    if (agent == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findWorks(agent.getUuid(), pageRequest);
  }

  PageResponse<Work> findWorks(UUID agentUuid, PageRequest pageRequest) throws RepositoryException;

  default Set<DigitalObject> getDigitalObjects(A agent) throws RepositoryException {
    if (agent == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getDigitalObjects(agent.getUuid());
  }

  // FIXME: replace with pagerequest method
  Set<DigitalObject> getDigitalObjects(UUID uuidAgent) throws RepositoryException;

  default Set<Work> getWorks(A agent) throws RepositoryException {
    if (agent == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getWorks(agent.getUuid());
  }

  // FIXME: replace with pagerequest method
  Set<Work> getWorks(UUID uuidAgent) throws RepositoryException;
}
