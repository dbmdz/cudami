package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation.EntityToEntityRelationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.LocalDateRangeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.TitleMapper;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.Title;
import de.digitalcollections.model.time.LocalDateRange;
import de.digitalcollections.model.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository("workRepository")
public class WorkRepositoryImpl extends EntityRepositoryImpl<Work> implements WorkRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WorkRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "wo";
  public static final String TABLE_ALIAS = "w";
  public static final String TABLE_NAME = "works";

  private AgentRepositoryImpl<Agent> agentRepository;
  private EntityToEntityRelationRepositoryImpl entityRelationRepository;
  private EntityRepositoryImpl<Entity> entityRepository;
  private HumanSettlementRepositoryImpl humanSettlementRepository;
  private ItemRepositoryImpl itemRepository;
  private ManifestationRepositoryImpl manifestationRepository;
  private PersonRepositoryImpl personRepository;

  public WorkRepositoryImpl(
      Jdbi jdbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository,
      LocalDateRangeMapper dateRangeMapper,
      TitleMapper titleMapper,
      EntityRepositoryImpl<Entity> entityRepository,
      AgentRepositoryImpl<Agent> agentRepository,
      HumanSettlementRepositoryImpl humanSettlementRepository,
      ManifestationRepositoryImpl manifestationRepository,
      ItemRepositoryImpl itemRepository,
      PersonRepositoryImpl personRepository,
      EntityToEntityRelationRepositoryImpl entityRelationRepository) {
    super(
        jdbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Work.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
    dbi.registerArgument(dateRangeMapper);
    dbi.registerColumnMapper(LocalDateRange.class, dateRangeMapper);
    dbi.registerColumnMapper(Title.class, titleMapper);

    this.entityRepository = entityRepository;
    this.agentRepository = agentRepository;
    this.humanSettlementRepository = humanSettlementRepository;
    this.manifestationRepository = manifestationRepository;
    this.itemRepository = itemRepository;
    this.personRepository = personRepository;
    this.entityRelationRepository = entityRelationRepository;
  }

  @Override
  public Work create() throws RepositoryException {
    return new Work();
  }

  @Override
  protected void basicReduceRowsBiConsumer(Map<UUID, Work> map, RowView rowView) {
    super.basicReduceRowsBiConsumer(map, rowView);
    Work work = map.get(rowView.getColumn(mappingPrefix + "_uuid", UUID.class));

    // parents
    UUID parentUuid = rowView.getColumn("parent_uuid", UUID.class);
    if (parentUuid != null) {
      if (work.getParents() == null) {
        work.setParents(new ArrayList<>(1));
      }
      if (!work.getParents().stream()
          .anyMatch(relSpec -> Objects.equals(relSpec.getUuid(), parentUuid))) {
        Work parent =
            Work.builder()
                .uuid(parentUuid)
                .label(rowView.getColumn("parent_label", LocalizedText.class))
                .titles(rowView.getColumn("parent_titles", new GenericType<List<Title>>() {}))
                .refId(rowView.getColumn("parent_refId", Integer.class))
                .notes(
                    rowView.getColumn(
                        "parent_notes", new GenericType<List<LocalizedStructuredContent>>() {}))
                .created(rowView.getColumn("parent_created", LocalDateTime.class))
                .lastModified(rowView.getColumn("parent_lastModified", LocalDateTime.class))
                .identifiableObjectType(
                    rowView.getColumn(
                        "parent_identifiableObjectType", IdentifiableObjectType.class))
                .identifiers(
                    rowView.getColumn("parent_identifiers", new GenericType<Set<Identifier>>() {}))
                .build();
        work.getParents().add(parent);
      }
    }

    // relations
    UUID entityUuid = rowView.getColumn(entityRepository.getMappingPrefix() + "_uuid", UUID.class);
    if (entityUuid != null) {
      if (work.getRelations() == null || work.getRelations().isEmpty()) {
        int maxIndex = rowView.getColumn("relation_max_sortindex", Integer.class);
        Vector<EntityRelation> relations = new Vector<>(++maxIndex);
        relations.setSize(maxIndex);
        work.setRelations(relations);
      }
      String relationPredicate =
          rowView.getColumn(
              EntityToEntityRelationRepositoryImpl.MAPPING_PREFIX + "_predicate", String.class);
      if (!work.getRelations().stream()
          .anyMatch(
              relation ->
                  relation != null
                      && Objects.equals(entityUuid, relation.getSubject().getUuid())
                      && Objects.equals(relationPredicate, relation.getPredicate()))) {
        Entity relatedEntity = rowView.getRow(Entity.class);
        work.getRelations()
            .set(
                rowView.getColumn(
                    EntityToEntityRelationRepositoryImpl.MAPPING_PREFIX + "_sortindex",
                    Integer.class),
                EntityRelation.builder()
                    .subject(relatedEntity)
                    .predicate(relationPredicate)
                    .additionalPredicates(
                        rowView.getColumn(
                            EntityToEntityRelationRepositoryImpl.MAPPING_PREFIX
                                + "_additionalPredicates",
                            new GenericType<List<String>>() {}))
                    .build());
      }
    }
  }

  @Override
  public PageResponse<Work> findByPerson(UUID personUuid) {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  public PageResponse<Work> findEmbeddedWorks(UUID uuid, PageRequest pageRequest)
      throws RepositoryException {
    final String workWorksTableName = "work_works";
    final String workWorksTableAlias = "wws";

    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + workWorksTableName
                + " AS "
                + workWorksTableAlias
                + " INNER JOIN "
                + tableName
                + " "
                + tableAlias
                + " ON "
                + workWorksTableAlias
                + ".object_uuid = "
                + tableAlias
                + ".uuid"
                + " WHERE "
                + workWorksTableAlias
                + ".subject_uuid = :subject_uuid");

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("subject_uuid", uuid);

    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".* " + commonSql);
    addPagingAndSorting(pageRequest, innerQuery);
    List<Work> result =
        retrieveList(
            getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            getOrderBy(pageRequest.getSorting()));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> orderByFields = super.getAllowedOrderByFields();
    return orderByFields;
  }

  @Override
  public Work getByItem(UUID itemUuid) throws RepositoryException {
    String innerSelect =
        " (SELECT w.* FROM "
            + getTableName()
            + " "
            + getTableAlias()
            + ", "
            + manifestationRepository.getTableName()
            + " "
            + manifestationRepository.getTableAlias()
            + ", "
            + itemRepository.getTableName()
            + " "
            + itemRepository.getTableAlias()
            + " WHERE "
            + itemRepository.getTableAlias()
            + ".uuid = :item_uuid"
            + " AND "
            + itemRepository.getTableAlias()
            + ".manifestation="
            + manifestationRepository.getTableAlias()
            + ".uuid"
            + " AND "
            + manifestationRepository.getTableAlias()
            + ".work="
            + getTableAlias()
            + ".uuid)";

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("item_uuid", itemUuid);

    return retrieveOne(getSqlSelectAllFields(), null, null, argumentMappings, innerSelect);
  }

  @Override
  public Set<Work> getByPerson(UUID personUuid) throws RepositoryException {
    StringBuilder innerSelect =
        new StringBuilder(
            " SELECT "
                + getTableAlias()
                + ".* FROM "
                + getTableName()
                + " "
                + getTableAlias()
                + ", "
                + personRepository.getTableName()
                + " "
                + personRepository.getTableAlias()
                + ", "
                + entityRelationRepository.getTableName()
                + " "
                + entityRelationRepository.getTableAlias()
                + " WHERE "
                + personRepository.getTableAlias()
                + ".uuid = :person_uuid"
                + " AND "
                + entityRelationRepository.getTableAlias()
                + ".subject_uuid="
                + personRepository.getTableAlias()
                + ".uuid"
                + " AND "
                + entityRelationRepository.getTableAlias()
                + ".object_uuid="
                + getTableAlias()
                + ".uuid");

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("person_uuid", personUuid);
    return new HashSet<>(
        retrieveList(getSqlSelectReducedFields(), innerSelect, argumentMappings, null));
  }

  @Override
  public String getColumnName(String modelProperty) {
    switch (modelProperty) {
      case "titles":
        return modelProperty;
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", creation_daterange"
        + ", creation_timevalue"
        + ", first_appeared_date"
        + ", first_appeared_presentation"
        + ", first_appeared_timevalue"
        + ", titles";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :creationDateRange::daterange"
        + ", :creationTimeValue::JSONB"
        + ", :firstAppearedDate"
        + ", :firstAppearedDatePresentation"
        + ", :firstAppearedTimeValue::JSONB"
        + ", {{titles}}";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
            , %1$s.creation_daterange %2$s_creationDateRange
            , %1$s.creation_timevalue %2$s_creationTimeValue
            , %1$s.first_appeared_date %2$s_firstAppearedDate
            , %1$s.first_appeared_presentation %2$s_firstAppearedDatePresentation
            , %1$s.first_appeared_timevalue %2$s_firstAppearedTimeValue
            , %1$s.titles %2$s_titles,
            """
            .formatted(tableAlias, mappingPrefix)
        // parents
        + """
            parent.uuid parent_uuid, parent.label parent_label, parent.titles parent_titles,
            parent.refid parent_refId, parent.notes parent_notes, parent.created parent_created, parent.last_modified parent_lastModified,
            parent.identifiable_objecttype parent_identifiableObjectType, get_identifiers(parent.uuid) parent_identifiers,
            """
        // relations
        + """
            {{entityRelationAlias}}.predicate {{entityRelationMap}}_predicate, {{entityRelationAlias}}.sortindex {{entityRelationMap}}_sortindex,
            {{entityRelationAlias}}.additional_predicates {{entityRelationMap}}_additionalPredicates,
            max({{entityRelationAlias}}.sortindex) OVER (PARTITION BY {{tableAlias}}.uuid) relation_max_sortindex,
            get_identifiers({{entityAlias}}.uuid) {{entityMapping}}_identifiers,
            """
            .replace("{{tableAlias}}", tableAlias)
            .replace("{{entityRelationAlias}}", EntityToEntityRelationRepositoryImpl.TABLE_ALIAS)
            .replace("{{entityRelationMap}}", EntityToEntityRelationRepositoryImpl.MAPPING_PREFIX)
            .replace("{{entityAlias}}", entityRepository.getTableAlias())
            .replace("{{entityMapping}}", entityRepository.getMappingPrefix())
        + entityRepository.getSqlSelectReducedFields();
  }

  @Override
  protected String getSqlSelectReducedFieldsJoins() {
    return super.getSqlSelectReducedFieldsJoins()
        + """
      LEFT JOIN (
        work_works wws INNER JOIN works parent
        ON parent.uuid = wws.subject_uuid
      ) ON wws.object_uuid = %1$s.uuid
      LEFT JOIN (
        %2$s %3$s INNER JOIN %4$s %5$s ON %3$s.subject_uuid = %5$s.uuid
      ) ON %3$s.object_uuid = %1$s.uuid
      """
            .formatted(
                tableAlias,
                /*2-3*/ EntityToEntityRelationRepositoryImpl.TABLE_NAME,
                EntityToEntityRelationRepositoryImpl.TABLE_ALIAS,
                /*4-5*/ EntityRepositoryImpl.TABLE_NAME,
                EntityRepositoryImpl.TABLE_ALIAS);
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", creation_daterange=:creationDateRange::daterange"
        + ", creation_timevalue=:creationTimeValue::JSONB"
        + ", first_appeared_date=:firstAppearedDate"
        + ", first_appeared_presentation=:firstAppearedDatePresentation"
        + ", first_appeared_timevalue=:firstAppearedTimeValue::JSONB"
        + ", titles={{titles}}";
  }

  @Override
  public void save(Work work, Map<String, Object> bindings)
      throws RepositoryException, ValidationException {
    if (bindings == null) {
      bindings = new HashMap<>(3);
    }
    super.save(work, bindings, TitleSqlHelper.buildTitleSql(work.getTitles()));
    saveParents(work);
  }

  private void saveParents(Work work) {
    if (work == null) return;
    /* - subject (subject_uuid) is the parent (superior work)
     * - object (object_uuid) is the child, i.e. this work parameter
     */
    dbi.useHandle(
        h ->
            h.createUpdate("DELETE FROM work_works WHERE object_uuid = :uuid")
                .bind("uuid", work.getUuid())
                .execute());

    if (work.getParents() == null || work.getParents().isEmpty()) return;

    dbi.useHandle(
        h -> {
          PreparedBatch batch =
              h.prepareBatch(
                  """
          INSERT INTO work_works (
            subject_uuid, object_uuid
          ) VALUES (
            :subject, :object
          )""");
          for (Work parent : work.getParents()) {
            if (parent.getUuid() == null) continue;

            batch.bind("object", work.getUuid()).bind("subject", parent.getUuid()).add();
          }
          batch.execute();
        });
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "firstAppearedDatePresentation":
        return true;
      default:
        return super.supportsCaseSensitivityForProperty(modelProperty);
    }
  }

  @Override
  public void update(Work work, Map<String, Object> bindings)
      throws RepositoryException, ValidationException {
    if (bindings == null) {
      bindings = new HashMap<>(3);
    }
    super.update(work, bindings, TitleSqlHelper.buildTitleSql(work.getTitles()));
    saveParents(work);
  }
}
