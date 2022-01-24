package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.HeadwordEntryRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.semantic.HeadwordRepositoryImpl;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.semantic.Headword;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class HeadwordEntryRepositoryImpl extends EntityRepositoryImpl<HeadwordEntry>
    implements HeadwordEntryRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordEntryRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "he";
  private static final String SQL_SELECT_ALL_FIELDS_JOINS =
      " LEFT JOIN "
          + HeadwordRepositoryImpl.TABLE_NAME
          + " AS "
          + HeadwordRepositoryImpl.TABLE_ALIAS
          + " ON "
          + HeadwordRepositoryImpl.TABLE_ALIAS
          + ".uuid = he.headword";
  public static final String TABLE_ALIAS = "he";
  public static final String TABLE_NAME = "headwordentries";

  private static BiFunction<Map<UUID, HeadwordEntry>, RowView, Map<UUID, HeadwordEntry>>
      createAdditionalReduceRowsBiFunction() {
    return (map, rowView) -> {
      // entity should be already in map, as we here just add additional data
      HeadwordEntry headwordEntry =
          map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));

      if (rowView.getColumn(HeadwordRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class) != null) {
        UUID headwordUuid =
            rowView.getColumn(HeadwordRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class);
        String label =
            rowView.getColumn(HeadwordRepositoryImpl.MAPPING_PREFIX + "_label", String.class);
        final Headword headword = new Headword();
        headword.setUuid(headwordUuid);
        headword.setLabel(label);
        headwordEntry.setHeadword(headword);
      }
      return map;
    };
  }

  public static String getSqlInsertFields() {
    return ArticleRepositoryImpl.getSqlInsertFields() + ", headword";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return ArticleRepositoryImpl.getSqlInsertValues() + ", :headword";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text"
        + ", "
        + HeadwordRepositoryImpl.TABLE_ALIAS
        + ".uuid "
        + HeadwordRepositoryImpl.MAPPING_PREFIX
        + "_uuid"
        + ", "
        + HeadwordRepositoryImpl.TABLE_ALIAS
        + ".label "
        + HeadwordRepositoryImpl.MAPPING_PREFIX
        + "_label";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return ArticleRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlUpdateFieldValues() {
    return ArticleRepositoryImpl.getSqlUpdateFieldValues() + ", headword=:headword";
  }

  private final EntityRepositoryImpl<Entity> entityRepositoryImpl;

  @Autowired
  public HeadwordEntryRepositoryImpl(
      Jdbi dbi,
      @Qualifier("entityRepositoryImpl") EntityRepositoryImpl<Entity> entityRepositoryImpl) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        HeadwordEntry.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues(),
        SQL_SELECT_ALL_FIELDS_JOINS,
        createAdditionalReduceRowsBiFunction());
    this.entityRepositoryImpl = entityRepositoryImpl;
  }

  @Override
  public List<HeadwordEntry> findByHeadword(UUID headwordUuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " WHERE headword = :uuid"
                + " ORDER BY date_published ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", headwordUuid);

    List<HeadwordEntry> result =
        retrieveList(
            sqlSelectReducedFields,
            innerQuery,
            argumentMappings,
            "ORDER BY " + tableAlias + ".date_published ASC");
    return result;
  }

  @Override
  public HeadwordEntry findOne(UUID uuid, Filtering filtering) {
    HeadwordEntry headwordEntry = super.findOne(uuid, filtering);

    if (headwordEntry != null) {
      List<Agent> creators = getCreators(uuid);
      headwordEntry.setCreators(creators);
    }
    return headwordEntry;
  }

  @Override
  public HeadwordEntry findOne(Identifier identifier) {
    HeadwordEntry headwordEntry = super.findOne(identifier);

    if (headwordEntry != null) {
      headwordEntry.setCreators(getCreators(headwordEntry.getUuid()));
    }
    return headwordEntry;
  }

  @Override
  public HeadwordEntry findOneByRefId(long refId) {
    HeadwordEntry headwordEntry = super.findOneByRefId(refId);

    if (headwordEntry != null) {
      headwordEntry.setCreators(getCreators(headwordEntry.getUuid()));
    }
    return headwordEntry;
  }

  @Override
  public List<Agent> getCreators(UUID headwordEntryUuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT hec.sortindex AS idx, * FROM "
                + EntityRepositoryImpl.TABLE_NAME
                + " AS e"
                + " LEFT JOIN headwordentry_creators AS hec ON e.uuid = hec.agent_uuid"
                + " WHERE hec.headwordentry_uuid = :uuid"
                + " ORDER BY hec.sortindex ASC");
    final String fieldsSql = entityRepositoryImpl.getSqlSelectReducedFields();

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", headwordEntryUuid);
    List<Entity> entityList =
        entityRepositoryImpl.retrieveList(
            fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC");

    List<Agent> agents = null;
    if (entityList != null) {
      agents =
          entityList.stream()
              .map(
                  (entity) -> {
                    // FIXME: use new agentrepositoryimpl (see workrepositoryimpl)
                    EntityType entityType = entity.getEntityType();
                    switch (entityType) {
                      case CORPORATE_BODY:
                        CorporateBody corporateBody = new CorporateBody();
                        corporateBody.setLabel(entity.getLabel());
                        corporateBody.setRefId(entity.getRefId());
                        corporateBody.setUuid(entity.getUuid());
                        return corporateBody;
                      case FAMILY:
                        Family family = new Family();
                        family.setLabel(entity.getLabel());
                        family.setRefId(entity.getRefId());
                        family.setUuid(entity.getUuid());
                        return family;
                      case PERSON:
                        Person person = new Person();
                        person.setLabel(entity.getLabel());
                        person.setRefId(entity.getRefId());
                        person.setUuid(entity.getUuid());
                        return person;
                      default:
                        return null;
                    }
                  })
              .collect(Collectors.toList());
    }

    return agents;
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityUuid) {
    return entityRepositoryImpl.getRelatedFileResources(entityUuid);
  }

  @Override
  public HeadwordEntry save(HeadwordEntry headwordEntry) {
    Map<String, Object> bindings = new HashMap<>();
    UUID headwordUuid = null;
    if (headwordEntry.getHeadword() != null) {
      headwordUuid = headwordEntry.getHeadword().getUuid();
    }
    bindings.put("headword", headwordUuid);
    super.save(headwordEntry, bindings);

    // save creators
    List<Agent> creators = headwordEntry.getCreators();
    saveCreatorsList(headwordEntry, creators);

    HeadwordEntry result = findOne(headwordEntry.getUuid());
    return result;
  }

  private void saveCreatorsList(HeadwordEntry headwordEntry, List<Agent> creators) {
    UUID headwordEntryUuid = headwordEntry.getUuid();

    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM headwordentry_creators WHERE headwordentry_uuid = :uuid")
                .bind("uuid", headwordEntryUuid)
                .execute());

    if (creators != null) {
      // second: save relations
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO headwordentry_creators(headwordentry_uuid, agent_uuid, sortIndex) VALUES(:uuid, :agentUuid, :sortIndex)");
            for (Agent agent : creators) {
              preparedBatch
                  .bind("uuid", headwordEntryUuid)
                  .bind("agentUuid", agent.getUuid())
                  .bind("sortIndex", getIndex(creators, agent))
                  .add();
            }
            preparedBatch.execute();
          });
    }
  }

  @Override
  public HeadwordEntry update(HeadwordEntry headwordEntry) {
    Map<String, Object> bindings = new HashMap<>();
    UUID headwordUuid = null;
    if (headwordEntry.getHeadword() != null) {
      headwordUuid = headwordEntry.getHeadword().getUuid();
    }
    bindings.put("headword", headwordUuid);
    super.update(headwordEntry, bindings);

    // save creators
    List<Agent> creators = headwordEntry.getCreators();
    saveCreatorsList(headwordEntry, creators);

    HeadwordEntry result = findOne(headwordEntry.getUuid());
    return result;
  }
}
