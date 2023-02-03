package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.AgentRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.entity.work.Work;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/** Repository for Agent persistence handling. No own table, using entities table. */
@Repository("agentRepository")
public class AgentRepositoryImpl<A extends Agent> extends EntityRepositoryImpl<A>
    implements AgentRepository<A> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "ag";
  public static final String TABLE_ALIAS = "ag";
  public static final String TABLE_NAME = "agents";

  @Autowired
  public AgentRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Agent.class,
        null,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  public AgentRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends Agent> agentImplClass,
      BiConsumer<Map<UUID, A>, RowView> additionalReduceRowsBiConsumer,
      int offsetForAlternativePaging) {
    super(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        agentImplClass,
        additionalReduceRowsBiConsumer,
        offsetForAlternativePaging);
  }

  @Override
  public Set<DigitalObject> getDigitalObjects(UUID uuidAgent) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Set<Work> getWorks(UUID uuidAgent) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}
