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

/** Repository for Agent persistence handling. No own table, using entities table. */
public class AgentRepositoryImpl extends EntityRepositoryImpl<Agent> implements AgentRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "ag";

  public static final String SQL_REDUCED_FIELDS_AG =
      " e.uuid ag_uuid, e.refid ag_refId, e.label ag_label, e.description ag_description,"
          + " e.identifiable_type ag_type, e.entity_type ag_entityType,"
          + " e.created ag_created, e.last_modified ag_lastModified,"
          + " e.preview_hints ag_previewImageRenderingHints";

  public static final String SQL_FULL_FIELDS_AG = SQL_REDUCED_FIELDS_AG;

  public static final String TABLE_ALIAS = "e";
  public static final String TABLE_NAME = "entities";

  @Autowired
  public AgentRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        AgentImpl.class,
        SQL_REDUCED_FIELDS_AG,
        SQL_FULL_FIELDS_AG);
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
