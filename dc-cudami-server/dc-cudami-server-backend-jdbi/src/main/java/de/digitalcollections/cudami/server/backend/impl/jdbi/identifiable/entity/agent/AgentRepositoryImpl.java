package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.AgentRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** Repository for Agent persistence handling. No own table, using entities table. */
@Repository
public class AgentRepositoryImpl extends EntityRepositoryImpl<Agent> implements AgentRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "ag";
  public static final String TABLE_ALIAS = "ag";
  public static final String TABLE_NAME = "agents";

  @Autowired
  public AgentRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Agent.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID uuidAgent) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Set<Work> getWorks(UUID uuidAgent) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Agent save(Agent agent) {
    super.save(agent);
    Agent result = getByUuid(agent.getUuid());
    return result;
  }

  @Override
  public Agent update(Agent agent) {
    super.update(agent);
    Agent result = getByUuid(agent.getUuid());
    return result;
  }
}
