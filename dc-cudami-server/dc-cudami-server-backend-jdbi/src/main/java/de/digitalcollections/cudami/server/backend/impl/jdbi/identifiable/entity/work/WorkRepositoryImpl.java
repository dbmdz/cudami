package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.work.Item;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.filtering.Filtering;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("workRepository")
public class WorkRepositoryImpl<W extends Work> extends EntityRepositoryImpl<W>
    implements WorkRepository<W> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "wo";
  public static final String TABLE_ALIAS = "w";
  public static final String TABLE_NAME = "works";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", date_published, timevalue_published";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :datePublished, :timeValuePublished::JSONB";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".date_published "
        + mappingPrefix
        + "_datePublished, "
        + tableAlias
        + ".timevalue_published "
        + mappingPrefix
        + "_timeValuePublished";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", date_published=:datePublished, timevalue_published=:timeValuePublished::JSONB";
  }

  private final AgentRepositoryImpl<Agent> agentRepositoryImpl;
  private final ItemRepositoryImpl itemRepositoryImpl;

  @Autowired
  public WorkRepositoryImpl(
      Jdbi dbi,
      @Qualifier("agentRepository") AgentRepositoryImpl<Agent> agentRepositoryImpl,
      ItemRepositoryImpl itemRepositoryImpl,
      CudamiConfig cudamiConfig) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Work.class,
        agentRepositoryImpl,
        itemRepositoryImpl,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  public WorkRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends Work> workImplClass,
      AgentRepositoryImpl<Agent> agentRepositoryImpl,
      ItemRepositoryImpl itemRepositoryImpl,
      int offsetForAlternativePaging) {
    super(dbi, tableName, tableAlias, mappingPrefix, workImplClass, offsetForAlternativePaging);
    this.agentRepositoryImpl = agentRepositoryImpl;
    this.itemRepositoryImpl = itemRepositoryImpl;
  }

  @Override
  public W getByIdentifier(Identifier identifier) {
    W work = super.getByIdentifier(identifier);

    if (work != null) {
      List<Agent> creators = getCreators(work.getUuid());
      work.setCreators(creators);
    }
    return work;
  }

  @Override
  public W getByUuidAndFiltering(UUID uuid, Filtering filtering) {
    W work = super.getByUuidAndFiltering(uuid, filtering);

    if (work != null) {
      List<Agent> creators = getCreators(uuid);
      work.setCreators(creators);
    }
    return work;
  }

  @Override
  public List<Agent> getCreators(UUID workUuid) {
    final String agTableAlias = agentRepositoryImpl.getTableAlias();
    final String agTableName = agentRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT wc.sortindex AS idx, * FROM "
                + agTableName
                + " AS "
                + agTableAlias
                + " LEFT JOIN work_creators AS wc ON "
                + agTableAlias
                + ".uuid = wc.agent_uuid"
                + " WHERE wc.work_uuid = :uuid"
                + " ORDER BY idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", workUuid);

    List<Agent> result =
        agentRepositoryImpl.retrieveList(
            agentRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            "ORDER BY idx ASC");
    return result;
  }

  @Override
  public List<Item> getItems(UUID workUuid) {
    final String itTableAlias = itemRepositoryImpl.getTableAlias();
    final String itTableName = itemRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT iw.sortindex AS idx, * FROM "
                + itTableName
                + " AS "
                + itTableAlias
                + " LEFT JOIN item_works AS iw ON "
                + itTableAlias
                + ".uuid = iw.item_uuid"
                + " WHERE iw.work_uuid = :uuid"
                + " ORDER BY idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", workUuid);
    List<Item> result =
        itemRepositoryImpl.retrieveList(
            itemRepositoryImpl.getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            "ORDER BY idx ASC");
    return result;
  }

  @Override
  public W save(W work) {
    super.save(work);

    // save creators
    List<Agent> creators = work.getCreators();
    setCreatorsList(work, creators);

    W result = getByUuid(work.getUuid());
    return result;
  }

  private void setCreatorsList(W work, List<Agent> creators) {
    UUID workUuid = work.getUuid();

    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM work_creators WHERE work_uuid = :uuid")
                .bind("uuid", workUuid)
                .execute());

    if (creators != null) {
      // second: save relations
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO work_creators(work_uuid, agent_uuid, sortIndex) VALUES(:uuid, :agentUuid, :sortIndex)");
            for (Agent agent : creators) {
              preparedBatch
                  .bind("uuid", workUuid)
                  .bind("agentUuid", agent.getUuid())
                  .bind("sortIndex", getIndex(creators, agent))
                  .add();
            }
            preparedBatch.execute();
          });
    }
  }

  @Override
  public W update(W work) {
    super.update(work);

    // save creators
    List<Agent> creators = work.getCreators();
    setCreatorsList(work, creators);

    W result = getByUuid(work.getUuid());
    return result;
  }
}
