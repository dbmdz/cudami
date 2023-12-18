package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation.EntityToEntityRelationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.LocalDateRangeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.MainSubTypeMapper.ExpressionTypeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.TitleMapper;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.manifestation.DistributionInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.identifiable.entity.manifestation.Manifestation;
import de.digitalcollections.model.identifiable.entity.manifestation.ProductionInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.PublicationInfo;
import de.digitalcollections.model.identifiable.entity.manifestation.Publisher;
import de.digitalcollections.model.identifiable.entity.manifestation.PublishingInfo;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.Title;
import de.digitalcollections.model.time.LocalDateRange;
import de.digitalcollections.model.validation.ValidationException;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.stream.Stream;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.StatementException;
import org.springframework.stereotype.Repository;

@SuppressFBWarnings(
    value = "VA_FORMAT_STRING_USES_NEWLINE",
    justification = "Newline is OK in multiline strings")
@Repository
public class ManifestationRepositoryImpl extends EntityRepositoryImpl<Manifestation>
    implements ManifestationRepository {

  public static final String MAPPING_PREFIX = "mf";
  public static final String TABLE_ALIAS = "mf";
  public static final String TABLE_NAME = "manifestations";

  private static void fillPublishers(
      List<Publisher> publishers, final Agent publAgent, final HumanSettlement publPlace) {
    if (publishers == null || publishers.isEmpty()) return;
    // agent
    if (publAgent != null) {
      publishers.parallelStream()
          .filter(
              p ->
                  p != null
                      && p.getAgent() != null
                      && publAgent.getUuid().equals(p.getAgent().getUuid())
                      // only "empty" objects are needed, i.e. those w/o created and last_modified
                      && !(p.getAgent().getCreated() != null
                          || p.getAgent().getLastModified() != null))
          .forEach(p -> p.setAgent(publAgent));
    }

    // locations
    if (publPlace != null) {
      // list in list is so much fun
      publishers.parallelStream()
          .filter(p -> p != null && p.getLocations() != null && !p.getLocations().isEmpty())
          .map(Publisher::getLocations)
          // now we have a stream of List<HumanSettlement>
          .forEach(
              settlements -> {
                Optional<HumanSettlement> old =
                    settlements.parallelStream()
                        .filter(
                            s ->
                                s != null
                                    && publPlace.getUuid().equals(s.getUuid())
                                    && !(s.getCreated() != null || s.getLastModified() != null))
                        .findAny();
                if (old.isPresent()) {
                  settlements.replaceAll(s -> Objects.equals(s, old.get()) ? publPlace : s);
                }
              });
    }
  }

  private AgentRepositoryImpl<Agent> agentRepository;
  private EntityRepositoryImpl<Entity> entityRepository;

  private HumanSettlementRepositoryImpl humanSettlementRepository;

  public ManifestationRepositoryImpl(
      Jdbi jdbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository,
      ExpressionTypeMapper expressionTypeMapper,
      LocalDateRangeMapper dateRangeMapper,
      TitleMapper titleMapper,
      EntityRepositoryImpl<Entity> entityRepository,
      AgentRepositoryImpl<Agent> agentRepository,
      HumanSettlementRepositoryImpl humanSettlementRepository) {
    super(
        jdbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Manifestation.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
    dbi.registerArrayType(expressionTypeMapper);
    dbi.registerArgument(dateRangeMapper);
    dbi.registerColumnMapper(ExpressionType.class, expressionTypeMapper);
    dbi.registerColumnMapper(LocalDateRange.class, dateRangeMapper);
    dbi.registerColumnMapper(Title.class, titleMapper);

    this.entityRepository = entityRepository;
    this.agentRepository = agentRepository;
    this.humanSettlementRepository = humanSettlementRepository;
  }

  @Override
  public Manifestation create() throws RepositoryException {
    return new Manifestation();
  }

  @Override
  public PageResponse<Manifestation> findManifestationsByWork(
      UUID workUuid, PageRequest pageRequest) throws RepositoryException {
    final String manifestationTableAlias = getTableAlias();
    final String manifestationTableName = getTableName();

    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + manifestationTableName
                + " AS "
                + manifestationTableAlias
                + " WHERE "
                + manifestationTableAlias
                + ".work = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", workUuid);

    Filtering filtering = pageRequest.getFiltering();
    // as filtering has other target object type (item) than this repository
    // (manifestation)
    // we have to rename filter field names to target table alias and column names:
    mapFilterExpressionsToOtherTableColumnNames(filtering, this);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT * " + commonSql);
    addPagingAndSorting(pageRequest, innerQuery);
    List<Manifestation> result =
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
  public PageResponse<Manifestation> findSubParts(UUID uuid, PageRequest pageRequest)
      throws RepositoryException {
    final String xtable = "manifestation_manifestations";
    final String xtableAlias = "mms";

    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + xtable
                + " "
                + xtableAlias
                + " INNER JOIN "
                + tableName
                + " "
                + tableAlias
                + " ON "
                + xtableAlias
                + ".object_uuid = "
                + tableAlias
                + ".uuid"
                + " WHERE "
                + xtableAlias
                + ".subject_uuid = :subject_uuid");

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("subject_uuid", uuid);

    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".* " + commonSql);
    addPagingAndSorting(pageRequest, innerQuery);
    List<Manifestation> result =
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
  public String getColumnName(String modelProperty) {
    switch (modelProperty) {
      case "composition":
      case "dimensions":
      case "language":
      case "scale":
      case "titles":
      case "version":
      case "work":
        return modelProperty;
      case "expressionTypes":
      case "manifestationType":
      case "manufacturingType":
      case "mediaTypes":
      case "otherLanguages":
        return modelProperty.toLowerCase();
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public List<Locale> getLanguagesOfManifestationsForWork(UUID workUuid) {
    String manifestationTableAlias = getTableAlias();
    String manifestationTableName = getTableName();
    String sql =
        "SELECT DISTINCT jsonb_object_keys("
            + manifestationTableAlias
            + ".label) as languages"
            + " FROM "
            + manifestationTableName
            + " AS "
            + manifestationTableAlias
            + String.format(" WHERE %s.work = :work_uuid;", manifestationTableAlias);
    return this.dbi.withHandle(
        h -> h.createQuery(sql).bind("work_uuid", workUuid).mapTo(Locale.class).list());
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + """
        , composition, dimensions, expressiontypes,
        language, manifestationtype, manufacturingtype,
        mediatypes, otherlanguages,
        scale, version, work, titles,
        publication_info, publication_nav_date,
        production_info, production_nav_date,
        distribution_info, distribution_nav_date,
        publishing_info_agent_uuids, publishing_info_locations_uuids
        """;
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + """
        , :composition, :dimensions, :expressionTypes::mainsubtype[],
        :language, :manifestationType, :manufacturingType,
        :mediaTypes::varchar[], :otherLanguages::varchar[],
        :scale, :version, :work?.uuid, {{titles}},
        :publicationInfo::jsonb, :publicationInfo?.navDateRange::daterange,
        :productionInfo::jsonb, :productionInfo?.navDateRange::daterange,
        :distributionInfo::jsonb, :distributionInfo?.navDateRange::daterange,
        :publishingInfoAgentUuids, :publishingInfoLocationsUuids
        """;
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectAllFields(tableAlias, mappingPrefix)
        + """
        , %1$s.composition %2$s_composition, %1$s.dimensions %2$s_dimensions, %1$s.otherlanguages %2$s_otherLanguages,
        %1$s.scale %2$s_scale, %1$s.version %2$s_version
        """
            .formatted(tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
            , %1$s.expressiontypes %2$s_expressionTypes, %1$s.language %2$s_language, %1$s.manifestationtype %2$s_manifestationType,
            %1$s.manufacturingtype %2$s_manufacturingType, %1$s.mediatypes %2$s_mediaTypes,
            %1$s.titles %2$s_titles,
            -- work
            %3$s.uuid %4$s_uuid, get_identifiers(%3$s.uuid) %4$s_identifiers, %3$s.label %4$s_label,
            %3$s.titles %4$s_titles,
            """
            .formatted(
                tableAlias,
                mappingPrefix,
                WorkRepositoryImpl.TABLE_ALIAS,
                WorkRepositoryImpl.MAPPING_PREFIX)
        // parents
        + """
            mms.title parent_title, mms.sortKey parent_sortKey,
            parent.uuid parent_uuid, parent.label parent_label, parent.titles parent_titles, parent.manifestationtype parent_manifestationType,
            parent.refid parent_refId, parent.notes parent_notes, parent.created parent_created, parent.last_modified parent_lastModified,
            parent.identifiable_objecttype parent_identifiableObjectType, get_identifiers(parent.uuid) parent_identifiers,
            -- parent's work
            parentwork.uuid parentwork_uuid, get_identifiers(parentwork.uuid) parentwork_identifiers, parentwork.label parentwork_label,
            parentwork.titles parentwork_titles,
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
        + entityRepository.getSqlSelectReducedFields()
        // publishing infos
        + """
            , {{tableAlias}}.publication_info {{mappingPrefix}}_publicationInfo, {{tableAlias}}.production_info {{mappingPrefix}}_productionInfo,
            {{tableAlias}}.distribution_info {{mappingPrefix}}_distributionInfo,
            -- publisher
            {{agentFields}},
            {{humanSettlementFields}},
            get_identifiers({{agentAlias}}.uuid) {{agentMap}}_identifiers,
            get_identifiers({{humanSettleAlias}}.uuid) {{humanSettleMap}}_identifiers
            """
            .replace("{{tableAlias}}", tableAlias)
            .replace("{{mappingPrefix}}", mappingPrefix)
            .replace("{{agentFields}}", agentRepository.getSqlSelectReducedFields())
            .replace(
                "{{humanSettlementFields}}", humanSettlementRepository.getSqlSelectReducedFields())
            .replace("{{agentAlias}}", agentRepository.getTableAlias())
            .replace("{{agentMap}}", agentRepository.getMappingPrefix())
            .replace("{{humanSettleAlias}}", humanSettlementRepository.getTableAlias())
            .replace("{{humanSettleMap}}", humanSettlementRepository.getMappingPrefix());
  }

  @Override
  protected String getSqlSelectReducedFieldsJoins() {
    return super.getSqlSelectReducedFieldsJoins()
        + """
        LEFT JOIN (
          manifestation_manifestations mms INNER JOIN manifestations parent
          ON parent.uuid = mms.subject_uuid
        ) ON mms.object_uuid = %1$s.uuid
        LEFT JOIN %6$s parentwork ON parentwork.uuid = parent.work
        LEFT JOIN (
          %2$s %3$s INNER JOIN %4$s %5$s ON %3$s.subject_uuid = %5$s.uuid
        ) ON %3$s.object_uuid = %1$s.uuid
        LEFT JOIN %6$s %7$s ON %7$s.uuid = %1$s.work
        """
            .formatted(
                tableAlias,
                /* 2-3 */
                EntityToEntityRelationRepositoryImpl.TABLE_NAME,
                EntityToEntityRelationRepositoryImpl.TABLE_ALIAS,
                /* 4-5 */
                EntityRepositoryImpl.TABLE_NAME,
                EntityRepositoryImpl.TABLE_ALIAS,
                /* 6-7 */
                WorkRepositoryImpl.TABLE_NAME,
                WorkRepositoryImpl.TABLE_ALIAS)
        + """
        LEFT JOIN %2$s %3$s ON %3$s.uuid = ANY (%1$s.publishing_info_agent_uuids)
        LEFT JOIN %4$s %5$s ON %5$s.uuid = ANY (%1$s.publishing_info_locations_uuids)
        """
            .formatted(
                tableAlias,
                /* 2-3 Publisher agents */
                AgentRepositoryImpl.TABLE_NAME,
                AgentRepositoryImpl.TABLE_ALIAS,
                /* 4-5 Publisher locations */
                HumanSettlementRepositoryImpl.TABLE_NAME,
                HumanSettlementRepositoryImpl.TABLE_ALIAS);
  }

  @Override
  protected void basicReduceRowsBiConsumer(Map<UUID, Manifestation> map, RowView rowView) {
    super.basicReduceRowsBiConsumer(map, rowView);
    Manifestation manifestation = map.get(rowView.getColumn(mappingPrefix + "_uuid", UUID.class));

    // parents
    UUID parentUuid = rowView.getColumn("parent_uuid", UUID.class);
    if (parentUuid != null) {
      if (manifestation.getParents() == null) {
        manifestation.setParents(new ArrayList<>(1));
      }
      String parentTitle = rowView.getColumn("parent_title", String.class);
      if (!manifestation.getParents().parallelStream()
          .anyMatch(
              relSpec ->
                  Objects.equals(relSpec.getSubject().getUuid(), parentUuid)
                      && Objects.equals(relSpec.getTitle(), parentTitle))) {
        Work parentWork =
            rowView.getColumn("parentwork_uuid", UUID.class) != null
                ? Work.builder()
                    .uuid(rowView.getColumn("parentwork_uuid", UUID.class))
                    .identifiers(
                        rowView.getColumn(
                            "parentwork_identifiers", new GenericType<Set<Identifier>>() {}))
                    .label(rowView.getColumn("parentwork_label", LocalizedText.class))
                    .titles(
                        rowView.getColumn("parentwork_titles", new GenericType<List<Title>>() {}))
                    .build()
                : null;
        Manifestation parent =
            Manifestation.builder()
                .uuid(parentUuid)
                .label(rowView.getColumn("parent_label", LocalizedText.class))
                .titles(rowView.getColumn("parent_titles", new GenericType<List<Title>>() {}))
                .manifestationType(rowView.getColumn("parent_manifestationType", String.class))
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
                .work(parentWork)
                .build();
        manifestation
            .getParents()
            .add(
                RelationSpecification.<Manifestation>builder()
                    .title(parentTitle)
                    .sortKey(rowView.getColumn("parent_sortKey", String.class))
                    .subject(parent)
                    .build());
      }
    }

    // relations
    UUID entityUuid = rowView.getColumn(entityRepository.getMappingPrefix() + "_uuid", UUID.class);
    if (entityUuid != null) {
      if (manifestation.getRelations() == null || manifestation.getRelations().isEmpty()) {
        int maxIndex = rowView.getColumn("relation_max_sortindex", Integer.class);
        Vector<EntityRelation> relations = new Vector<>(++maxIndex);
        relations.setSize(maxIndex);
        manifestation.setRelations(relations);
      }
      String relationPredicate =
          rowView.getColumn(
              EntityToEntityRelationRepositoryImpl.MAPPING_PREFIX + "_predicate", String.class);
      if (!manifestation.getRelations().stream()
          .anyMatch(
              relation ->
                  relation != null
                      && Objects.equals(entityUuid, relation.getSubject().getUuid())
                      && Objects.equals(relationPredicate, relation.getPredicate()))) {
        Entity relatedEntity = rowView.getRow(Entity.class);
        manifestation
            .getRelations()
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

    // work
    if (manifestation.getWork() == null) {
      Work work = rowView.getRow(Work.class);
      if (work != null && work.getUuid() != null) manifestation.setWork(work);
    }

    // publishers
    Agent publAgent = null;
    if (rowView.getColumn(AgentRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class) != null) {
      Agent ag = rowView.getRow(Agent.class);
      publAgent =
          switch (ag.getIdentifiableObjectType()) {
            case CORPORATE_BODY -> DerivedAgentBuildHelper.build(ag, CorporateBody.class);
            case PERSON -> DerivedAgentBuildHelper.build(ag, Person.class);
            case FAMILY -> DerivedAgentBuildHelper.build(ag, Family.class);
            default -> ag;
          };
    }
    HumanSettlement publPlace =
        rowView.getColumn(HumanSettlementRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class)
                != null
            ? rowView.getRow(HumanSettlement.class)
            : null;
    if (manifestation.getDistributionInfo() != null)
      fillPublishers(manifestation.getDistributionInfo().getPublishers(), publAgent, publPlace);
    if (manifestation.getProductionInfo() != null)
      fillPublishers(manifestation.getProductionInfo().getPublishers(), publAgent, publPlace);
    if (manifestation.getPublicationInfo() != null)
      fillPublishers(manifestation.getPublicationInfo().getPublishers(), publAgent, publPlace);
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + """
            , composition=:composition, dimensions=:dimensions, expressiontypes=:expressionTypes::mainsubtype[],
            language=:language, manifestationtype=:manifestationType, manufacturingtype=:manufacturingType,
            mediatypes=:mediaTypes::varchar[], otherlanguages=:otherLanguages::varchar[],
            scale=:scale, version=:version, work=:work?.uuid, titles={{titles}},
            publication_info=:publicationInfo::jsonb, publication_nav_date=:publicationInfo?.navDateRange::daterange,
            production_info=:productionInfo::jsonb, production_nav_date=:productionInfo?.navDateRange::daterange,
            distribution_info=:distributionInfo::jsonb, distribution_nav_date=:distributionInfo?.navDateRange::daterange,
            publishing_info_agent_uuids=:publishingInfoAgentUuids, publishing_info_locations_uuids=:publishingInfoLocationsUuids
            """;
  }

  @SuppressWarnings("unchecked")
  private <P extends PublishingInfo> P reducePublisher(P publishingInfo)
      throws RepositoryException {
    if (publishingInfo == null) return null;
    P result;
    try {
      result = (P) publishingInfo.getClass().getConstructor().newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new RepositoryException("PublishingInfo cannot be instantiated", e);
    }

    result.setDatePresentation(publishingInfo.getDatePresentation());
    result.setNavDateRange(publishingInfo.getNavDateRange());
    result.setTimeValueRange(publishingInfo.getTimeValueRange());
    if (publishingInfo.getPublishers() == null) return result;
    List<Publisher> publishers =
        publishingInfo.getPublishers().stream()
            .filter(Objects::nonNull)
            .map(
                publ -> {
                  Publisher publisher = new Publisher();
                  publisher.setDatePresentation(publ.getDatePresentation());
                  if (publ.getAgent() != null) {
                    // substitute the agent
                    Agent agent;
                    try {
                      agent = publ.getAgent().getClass().getConstructor().newInstance();
                    } catch (InstantiationException
                        | IllegalAccessException
                        | IllegalArgumentException
                        | InvocationTargetException
                        | NoSuchMethodException
                        | SecurityException e) {
                      agent = new Agent();
                    }
                    agent.setUuid(publ.getAgent().getUuid());
                    publisher.setAgent(agent);
                  }
                  if (publ.getLocations() != null && !publ.getLocations().isEmpty()) {
                    // substitute the locations
                    List<HumanSettlement> locations =
                        publ.getLocations().stream()
                            .filter(
                                settlement -> settlement != null && settlement.getUuid() != null)
                            .<HumanSettlement>map(
                                settlement ->
                                    HumanSettlement.builder().uuid(settlement.getUuid()).build())
                            .toList();
                    publisher.setLocations(locations);
                  }
                  return publisher;
                })
            .toList();
    result.setPublishers(publishers);
    return result;
  }

  @Override
  public boolean removeParent(Manifestation manifestation, Manifestation parentManifestation)
      throws RepositoryException {
    final String sql =
        "DELETE FROM manifestation_manifestations WHERE subject_uuid=:subject_uuid and object_uuid=:object_uuid";
    try {
      return dbi.withHandle(
              h ->
                  h.createUpdate(sql)
                      .bind("object_uuid", manifestation.getUuid())
                      .bind("subject_uuid", parentManifestation.getUuid())
                      .execute())
          == 1;
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public void save(Manifestation manifestation, Map<String, Object> bindings)
      throws RepositoryException, ValidationException {
    if (bindings == null) {
      bindings = new HashMap<>(3);
    }

    DistributionInfo distributionInfo = manifestation.getDistributionInfo();
    manifestation.setDistributionInfo(reducePublisher(distributionInfo));
    ProductionInfo productionInfo = manifestation.getProductionInfo();
    manifestation.setProductionInfo(reducePublisher(productionInfo));
    PublicationInfo publicationInfo = manifestation.getPublicationInfo();
    manifestation.setPublicationInfo(reducePublisher(publicationInfo));

    setPublishingInfoBindings(bindings, distributionInfo, productionInfo, publicationInfo);
    super.save(manifestation, bindings, TitleSqlHelper.buildTitleSql(manifestation.getTitles()));
    saveParents(manifestation);

    manifestation.setDistributionInfo(distributionInfo);
    manifestation.setProductionInfo(productionInfo);
    manifestation.setPublicationInfo(publicationInfo);
  }

  private void saveParents(Manifestation manifestation) {
    if (manifestation == null) return;
    /*
     * - subject (subject_uuid) is the parent (superior manifestation) - object
     * (object_uuid) is the child, i.e. this manifestation parameter
     */
    dbi.useHandle(
        h ->
            h.createUpdate("DELETE FROM manifestation_manifestations WHERE object_uuid = :uuid")
                .bind("uuid", manifestation.getUuid())
                .execute());

    if (manifestation.getParents() == null || manifestation.getParents().isEmpty()) return;

    dbi.useHandle(
        h -> {
          PreparedBatch batch =
              h.prepareBatch(
                  """
          INSERT INTO manifestation_manifestations (
            subject_uuid, object_uuid, title, sortkey
          ) VALUES (
            :subject, :object, :title, :sortkey
          )""");
          for (RelationSpecification<Manifestation> parent : manifestation.getParents()) {
            if (parent.getSubject() == null || parent.getSubject().getUuid() == null) continue;

            batch
                .bind("object", manifestation.getUuid())
                .bind("subject", parent.getSubject().getUuid())
                .bind("title", parent.getTitle())
                .bind("sortkey", parent.getSortKey())
                .add();
          }
          batch.execute();
        });
  }

  private void setPublishingInfoBindings(
      Map<String, Object> bindings, PublishingInfo... publishingInfos) {
    if (bindings == null || publishingInfos.length < 1) return;
    bindings.put(
        "publishingInfoAgentUuids",
        extractUuids(
            Stream.of(publishingInfos)
                .filter(pinfo -> Objects.nonNull(pinfo) && Objects.nonNull(pinfo.getPublishers()))
                .map(p -> p.getPublishers().stream())
                .flatMap(s -> s)
                .filter(
                    publisher ->
                        Objects.nonNull(publisher) && Objects.nonNull(publisher.getAgent()))
                .map(Publisher::getAgent)
                .toList()));
    bindings.put(
        "publishingInfoLocationsUuids",
        extractUuids(
            Stream.of(publishingInfos)
                .filter(pinfo -> Objects.nonNull(pinfo) && Objects.nonNull(pinfo.getPublishers()))
                .map(p -> p.getPublishers().stream())
                .flatMap(s -> s)
                .filter(
                    publisher ->
                        Objects.nonNull(publisher) && Objects.nonNull(publisher.getLocations()))
                .flatMap(publisher -> publisher.getLocations().stream())
                .toList()));
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "composition":
      case "dimensions":
      case "language":
      case "manifestationType":
      case "manufacturingType":
      case "publishingDatePresentation":
      case "scale":
      case "version":
        return true;
      default:
        return super.supportsCaseSensitivityForProperty(modelProperty);
    }
  }

  @Override
  public void update(Manifestation manifestation, Map<String, Object> bindings)
      throws RepositoryException, ValidationException {
    if (bindings == null) {
      bindings = new HashMap<>(3);
    }

    DistributionInfo distributionInfo = manifestation.getDistributionInfo();
    manifestation.setDistributionInfo(reducePublisher(distributionInfo));
    ProductionInfo productionInfo = manifestation.getProductionInfo();
    manifestation.setProductionInfo(reducePublisher(productionInfo));
    PublicationInfo publicationInfo = manifestation.getPublicationInfo();
    manifestation.setPublicationInfo(reducePublisher(publicationInfo));

    setPublishingInfoBindings(bindings, distributionInfo, productionInfo, publicationInfo);
    super.update(manifestation, bindings, TitleSqlHelper.buildTitleSql(manifestation.getTitles()));
    saveParents(manifestation);

    manifestation.setDistributionInfo(distributionInfo);
    manifestation.setProductionInfo(productionInfo);
    manifestation.setPublicationInfo(publicationInfo);
  }
}
