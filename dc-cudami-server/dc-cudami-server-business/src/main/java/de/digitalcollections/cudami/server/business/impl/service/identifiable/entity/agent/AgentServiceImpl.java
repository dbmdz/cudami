package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.AgentRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.AgentService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.EntityServiceImpl;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** Service for Agent handling. */
@Service
public class AgentServiceImpl extends EntityServiceImpl<Agent> implements AgentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentServiceImpl.class);

  @Autowired
  public AgentServiceImpl(AgentRepository repository) {
    super(repository);
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID uuidAgent) {
    return ((AgentRepository) repository).getDigitalObjects(uuidAgent);
  }

  @Override
  public Set<Work> getWorks(UUID uuidAgent) {
    return ((AgentRepository) repository).getWorks(uuidAgent);
  }
}
