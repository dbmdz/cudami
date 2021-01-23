package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.work.Item;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.impl.identifiable.entity.work.WorkImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WorkRepositoryImpl extends EntityRepositoryImpl<Work> implements WorkRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "wo";
  public static final String TABLE_ALIAS = "w";
  public static final String TABLE_NAME = "works";

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return EntityRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix)
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

  private final AgentRepositoryImpl agentRepositoryImpl;
  private final ItemRepositoryImpl itemRepositoryImpl;

  @Autowired
  public WorkRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      AgentRepositoryImpl agentRepositoryImpl,
      ItemRepositoryImpl itemRepositoryImpl) {
    super(dbi, identifierRepository, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, WorkImpl.class);
    this.agentRepositoryImpl = agentRepositoryImpl;
    this.itemRepositoryImpl = itemRepositoryImpl;
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public Work findOne(UUID uuid, Filtering filtering) {
    Work work = super.findOne(uuid, filtering);

    if (work != null) {
      List<Agent> creators = getCreators(uuid);
      work.setCreators(creators);
    }
    return work;
  }

  @Override
  public Work findOne(Identifier identifier) {
    Work work = super.findOne(identifier);

    if (work != null) {
      List<Agent> creators = getCreators(work.getUuid());
      work.setCreators(creators);
    }
    return work;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "refId"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "refId":
        return tableAlias + ".refid";
      default:
        return null;
    }
  }

  @Override
  public List<Agent> getCreators(UUID workUuid) {
    final String agTableAlias = agentRepositoryImpl.getTableAlias();
    final String agTableName = agentRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + agTableName
                + " AS "
                + agTableAlias
                + " LEFT JOIN work_creators AS wc ON "
                + agTableAlias
                + ".uuid = wc.agent_uuid"
                + " WHERE wc.work_uuid = :uuid"
                + " ORDER BY wc.sortIndex ASC");

    List<Agent> result =
        agentRepositoryImpl.retrieveList(
            agentRepositoryImpl.getSqlReducedFields(), innerQuery, Map.of("uuid", workUuid));
    return result;
  }

  @Override
  public List<Item> getItems(UUID workUuid) {
    final String itTableAlias = itemRepositoryImpl.getTableAlias();
    final String itTableName = itemRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + itTableName
                + " AS "
                + itTableAlias
                + " LEFT JOIN item_works AS iw ON "
                + itTableAlias
                + ".uuid = iw.item_uuid"
                + " WHERE iw.work_uuid = :uuid"
                + " ORDER BY iw.sortIndex ASC");

    List<Item> result =
        itemRepositoryImpl.retrieveList(
            itemRepositoryImpl.getSqlReducedFields(), innerQuery, Map.of("uuid", workUuid));
    return result;
  }

  @Override
  public Work save(Work work) {
    work.setUuid(UUID.randomUUID());
    work.setCreated(LocalDateTime.now());
    work.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        work.getPreviewImage() == null ? null : work.getPreviewImage().getUuid();

    String query =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints, custom_attrs,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " date_published , timevalue_published"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB, :customAttributes::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :datePublished, :timeValuePublished::JSONB"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(work)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = work.getIdentifiers();
    saveIdentifiers(identifiers, work);

    // save creators
    List<Agent> creators = work.getCreators();
    saveCreatorsList(work, creators);

    Work result = findOne(work.getUuid());
    return result;
  }

  private void saveCreatorsList(Work work, List<Agent> creators) {
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
  public Work update(Work work) {
    work.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        work.getPreviewImage() == null ? null : work.getPreviewImage().getUuid();

    String query =
        "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB, previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
            + " last_modified=:lastModified,"
            + " date_published=:datePublished , timevalue_published=:timeValuePublished::JSONB"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(work)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(work);
    Set<Identifier> identifiers = work.getIdentifiers();
    saveIdentifiers(identifiers, work);

    // save creators
    List<Agent> creators = work.getCreators();
    saveCreatorsList(work, creators);

    Work result = findOne(work.getUuid());
    return result;
  }
}
