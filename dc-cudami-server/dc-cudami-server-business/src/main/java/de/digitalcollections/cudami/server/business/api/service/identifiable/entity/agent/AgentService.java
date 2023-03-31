package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.List;
import java.util.Set;

public interface AgentService<A extends Agent> extends EntityService<A> {

  List<A> getCreatorsForWork(Work work) throws ServiceException;

  Set<DigitalObject> getDigitalObjects(A agent) throws ServiceException;

  Set<Work> getWorks(A agent) throws ServiceException;
}
