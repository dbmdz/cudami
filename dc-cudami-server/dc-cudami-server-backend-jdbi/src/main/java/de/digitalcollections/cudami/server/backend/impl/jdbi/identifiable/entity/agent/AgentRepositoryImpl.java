package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.AgentRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.impl.identifiable.entity.agent.AgentImpl;
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
  public static final String TABLE_ALIAS = "e";
  public static final String TABLE_NAME = "entities";

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Autowired
  public AgentRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, AgentImpl.class);
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
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
