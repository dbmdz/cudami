package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.AgentRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/** Repository for Agent persistence handling. No own table, using entities table. */
@Repository("agentRepository")
public class AgentRepositoryImpl<A extends Agent> extends EntityRepositoryImpl<A>
    implements AgentRepository<A> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "ag";
  public static final String TABLE_ALIAS = "ag";
  public static final String TABLE_NAME = "agents";

  public AgentRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Agent.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  public AgentRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends Entity> entityImplClass,
      int offsetForAlternativePaging) {
    super(dbi, tableName, tableAlias, mappingPrefix, entityImplClass, offsetForAlternativePaging);
  }

  @Override
  public PageResponse<DigitalObject> findDigitalObjects(UUID agentUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public PageResponse<Work> findWorks(UUID agentUuid, PageRequest pageRequest) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID uuidAgent) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public Set<Work> getWorks(UUID uuidAgent) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }
}
