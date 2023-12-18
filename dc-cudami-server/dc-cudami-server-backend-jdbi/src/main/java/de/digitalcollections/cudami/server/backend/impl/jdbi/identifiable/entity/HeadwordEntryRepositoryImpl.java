package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.HeadwordEntryRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.semantic.HeadwordRepositoryImpl;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.semantic.Headword;
import de.digitalcollections.model.validation.ValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class HeadwordEntryRepositoryImpl extends EntityRepositoryImpl<HeadwordEntry>
    implements HeadwordEntryRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordEntryRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "he";
  public static final String TABLE_ALIAS = "he";
  public static final String TABLE_NAME = "headwordentries";

  private final EntityRepositoryImpl<Entity> entityRepositoryImpl;

  public HeadwordEntryRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      @Qualifier("entityRepositoryImpl") EntityRepositoryImpl<Entity> entityRepositoryImpl,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        HeadwordEntry.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
    this.entityRepositoryImpl = entityRepositoryImpl;
  }

  @Override
  public HeadwordEntry create() throws RepositoryException {
    return new HeadwordEntry();
  }

  @Override
  protected void fullReduceRowsBiConsumer(Map<UUID, HeadwordEntry> map, RowView rowView) {
    super.fullReduceRowsBiConsumer(map, rowView);
    // entity should be already in map, as we here just add additional data
    HeadwordEntry headwordEntry = map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));

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
  }

  @Override
  public List<HeadwordEntry> getByHeadword(UUID headwordUuid) throws RepositoryException {
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
            getSqlSelectAllFields(),
            getSqlSelectAllFieldsJoins(),
            innerQuery,
            argumentMappings,
            "ORDER BY " + tableAlias + ".date_published ASC");
    return result;
  }

  @Override
  public HeadwordEntry getByIdentifier(Identifier identifier) throws RepositoryException {
    HeadwordEntry headwordEntry = super.getByIdentifier(identifier);

    if (headwordEntry != null) {
      headwordEntry.setCreators(getCreators(headwordEntry.getUuid()));
    }
    return headwordEntry;
  }

  @Override
  public HeadwordEntry getByRefId(long refId) throws RepositoryException {
    HeadwordEntry headwordEntry = super.getByRefId(refId);

    if (headwordEntry != null) {
      headwordEntry.setCreators(getCreators(headwordEntry.getUuid()));
    }
    return headwordEntry;
  }

  @Override
  public List<HeadwordEntry> getByUuidsAndFiltering(List<UUID> uuids, Filtering filtering)
      throws RepositoryException {
    List<HeadwordEntry> headwordEntries = super.getByUuidsAndFiltering(uuids, filtering);

    Optional.ofNullable(headwordEntries)
        .map(List::parallelStream)
        .ifPresent(
            stream ->
                stream.forEach(
                    headwordEntry -> {
                      try {
                        headwordEntry.setCreators(getCreators(headwordEntry.getUuid()));
                      } catch (RepositoryException e) {
                        LOGGER.error(
                            "Cannot get creators of headword entry with UUID %s: %s"
                                .formatted(headwordEntry.getUuid(), e),
                            e);
                      }
                    }));
    return headwordEntries;
  }

  @Override
  public List<Agent> getCreators(UUID headwordEntryUuid) throws RepositoryException {
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
                    IdentifiableObjectType type = entity.getIdentifiableObjectType();
                    switch (type) {
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
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", date_published, text, timevalue_published, headword";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :datePublished, :text::JSONB, :timeValuePublished::JSONB, :headword";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectAllFields(tableAlias, mappingPrefix)
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

  @Override
  protected String getSqlSelectAllFieldsJoins() {
    return super.getSqlSelectAllFieldsJoins()
        + " LEFT JOIN "
        + HeadwordRepositoryImpl.TABLE_NAME
        + " AS "
        + HeadwordRepositoryImpl.TABLE_ALIAS
        + " ON "
        + HeadwordRepositoryImpl.TABLE_ALIAS
        + ".uuid = he.headword";
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
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", date_published=:datePublished, text=:text::JSONB, timevalue_published=:timeValuePublished::JSONB, headword=:headword";
  }

  @Override
  public void save(HeadwordEntry headwordEntry) throws RepositoryException, ValidationException {
    Map<String, Object> bindings = new HashMap<>();
    UUID headwordUuid = null;
    if (headwordEntry.getHeadword() != null) {
      headwordUuid = headwordEntry.getHeadword().getUuid();
    }
    bindings.put("headword", headwordUuid);
    super.save(headwordEntry, bindings);

    // save creators
    List<Agent> creators = headwordEntry.getCreators();
    setCreatorsList(headwordEntry, creators);
  }

  private void setCreatorsList(HeadwordEntry headwordEntry, List<Agent> creators) {
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
  public void update(HeadwordEntry headwordEntry) throws RepositoryException, ValidationException {
    Map<String, Object> bindings = new HashMap<>();
    UUID headwordUuid = null;
    if (headwordEntry.getHeadword() != null) {
      headwordUuid = headwordEntry.getHeadword().getUuid();
    }
    bindings.put("headword", headwordUuid);
    super.update(headwordEntry, bindings);

    // save creators
    List<Agent> creators = headwordEntry.getCreators();
    setCreatorsList(headwordEntry, creators);
  }
}
