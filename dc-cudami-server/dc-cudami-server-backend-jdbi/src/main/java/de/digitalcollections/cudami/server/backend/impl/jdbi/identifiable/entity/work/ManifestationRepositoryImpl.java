package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation.EntityRelationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.semantic.SubjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.LocalDateRangeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.MainSubTypeMapper.ExpressionTypeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.TitleMapper;
import de.digitalcollections.model.RelationSpecification;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
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
import de.digitalcollections.model.identifiable.entity.manifestation.Title;
import de.digitalcollections.model.identifiable.entity.relation.EntityRelation;
import de.digitalcollections.model.semantic.Subject;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.time.LocalDateRange;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;
import java.util.function.BiFunction;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.stereotype.Repository;

@SuppressFBWarnings(
    value = "VA_FORMAT_STRING_USES_NEWLINE",
    justification = "Newline is OK in multiline strings")
@Repository
public class ManifestationRepositoryImpl extends EntityRepositoryImpl<Manifestation>
    implements ManifestationRepository {

  public static final String TABLE_NAME = "manifestations";
  public static final String TABLE_ALIAS = "mf";
  public static final String MAPPING_PREFIX = "mf";

  private EntityRepositoryImpl<Entity> entityRepository;
  private AgentRepositoryImpl<Agent> agentRepository;
  private HumanSettlementRepositoryImpl humanSettlementRepository;

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + """
        , composition, dimensions, expressiontypes,
        language, manifestationtype, manufacturingtype,
        mediatypes, otherlanguages,
        scale, subjects_uuids, version,
        work, titles,
        publication_info, publication_nav_date,
        production_info, production_nav_date,
        distribution_info, distribution_nav_date
        """;
  }

  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + """
        , :composition, :dimensions, :expressionTypes::mainsubtype[],
        :language, :manifestationType, :manufacturingType,
        :mediaTypes::varchar[], :otherLanguages::varchar[],
        :scale, :subjects_uuids::UUID[], :version,
        :work?.uuid, {{titles}},
        :publicationInfo::jsonb, :publicationInfo?.navDateRange::daterange,
        :productionInfo::jsonb, :productionInfo?.navDateRange::daterange,
        :distributionInfo::jsonb, :distributionInfo?.navDateRange::daterange
        """;
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + """
        , composition=:composition, dimensions=:dimensions, expressiontypes=:expressionTypes::mainsubtype[],
        language=:language, manifestationtype=:manifestationType, manufacturingtype=:manufacturingType,
        mediatypes=:mediaTypes::varchar[], otherlanguages=:otherLanguages::varchar[],
        scale=:scale, subjects_uuids=:subjects_uuids::UUID[], version=:version,
        work=:work?.uuid, titles={{titles}},
        publication_info=:publicationInfo::jsonb, publication_nav_date=:publicationInfo?.navDateRange::daterange,
        production_info=:productionInfo::jsonb, production_nav_date=:productionInfo?.navDateRange::daterange,
        distribution_info=:distributionInfo::jsonb, distribution_nav_date=:distributionInfo?.navDateRange::daterange
        """;
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
          , %1$s.composition %2$s_composition, %1$s.dimensions %2$s_dimensions, %1$s.otherlanguages %2$s_otherLanguages,
          %1$s.scale %2$s_scale, %1$s.version %2$s_version,
          %1$s.publication_info %2$s_publicationInfo, %1$s.production_info %2$s_productionInfo,
          %1$s.distribution_info %2$s_distributionInfo,
          """
            .formatted(tableAlias, mappingPrefix)
        // subjects
        + SubjectRepositoryImpl.SQL_REDUCED_FIELDS_SUBJECTS
        // publishers
        + ", %s, %s"
            .formatted(
                agentRepository.getSqlSelectReducedFields(),
                humanSettlementRepository.getSqlSelectReducedFields());
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
            , %1$s.expressiontypes %2$s_expressionTypes, %1$s.language %2$s_language, %1$s.manifestationtype %2$s_manifestationType,
            %1$s.manufacturingtype %2$s_manufacturingType, %1$s.mediatypes %2$s_mediaTypes,
            %1$s.titles %2$s_titles,
            """
            .formatted(tableAlias, mappingPrefix)
        // parents
        + """
            mms.title parent_title, mms.sortKey parent_sortKey,
            parent.uuid parent_uuid, parent.label parent_label, parent.titles parent_titles, parent.manifestationtype parent_manifestationType,
            parent.refid parent_refId, parent.notes parent_notes, parent.created parent_created, parent.last_modified parent_lastModified,
            parent.identifiable_objecttype parent_identifiableObjectType,
            """
        // relations
        + """
          %1$s.predicate %2$s_predicate, %1$s.sortindex %2$s_sortindex,
          %1$s.additional_predicates %2$s_additionalPredicates,
          max(%1$s.sortindex) OVER (PARTITION BY %3$s.uuid) relation_max_sortindex, """
            .formatted(
                EntityRelationRepositoryImpl.TABLE_ALIAS,
                EntityRelationRepositoryImpl.MAPPING_PREFIX,
                tableAlias)
        + entityRepository.getSqlSelectReducedFields();
  }

  public static final String SQL_SELECT_ALL_FIELDS_JOINS =
      """
      LEFT JOIN %2$s %3$s ON %3$s.uuid = ANY (%1$s.subjects_uuids)
      LEFT JOIN (
        manifestation_manifestations mms INNER JOIN manifestations parent
        ON parent.uuid = mms.subject_uuid
      ) ON mms.object_uuid = %1$s.uuid
      LEFT JOIN (
        %4$s %5$s INNER JOIN %6$s %7$s ON %5$s.subject_uuid = %7$s.uuid
      ) ON %5$s.object_uuid = %1$s.uuid
      LEFT JOIN %8$s %9$s ON %9$s.uuid IN (
          select (a->>'uuid')::UUID from jsonb_path_query(jsonb_build_array(
              %1$s.publication_info, %1$s.production_info, %1$s.distribution_info
          ),
          '$[*].publishers[*].agent') agent(a))
      LEFT JOIN %10$s %11$s ON %11$s.uuid IN (
          select (a->>'uuid')::UUID from jsonb_path_query(jsonb_build_array(
              %1$s.publication_info, %1$s.production_info, %1$s.distribution_info
          ),
          '$[*].publishers[*].locations[*]') humansettlement(a))
      """
          .formatted(
              TABLE_ALIAS,
              /*2-3*/ SubjectRepositoryImpl.TABLE_NAME,
              SubjectRepositoryImpl.TABLE_ALIAS,
              /*4-5*/ EntityRelationRepositoryImpl.TABLE_NAME,
              EntityRelationRepositoryImpl.TABLE_ALIAS,
              /*6-7*/ EntityRepositoryImpl.TABLE_NAME,
              EntityRepositoryImpl.TABLE_ALIAS,
              /*8-9 Publisher agents*/
              AgentRepositoryImpl.TABLE_NAME,
              AgentRepositoryImpl.TABLE_ALIAS,
              /*10-11 Publisher locations*/ HumanSettlementRepositoryImpl.TABLE_NAME,
              HumanSettlementRepositoryImpl.TABLE_ALIAS);

  public ManifestationRepositoryImpl(
      Jdbi jdbi,
      CudamiConfig cudamiConfig,
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
        SQL_SELECT_ALL_FIELDS_JOINS,
        ManifestationRepositoryImpl::additionalReduceRowsBiConsumer,
        cudamiConfig.getOffsetForAlternativePaging());
    dbi.registerArrayType(expressionTypeMapper);
    dbi.registerArgument(dateRangeMapper);
    dbi.registerColumnMapper(ExpressionType.class, expressionTypeMapper);
    dbi.registerColumnMapper(LocalDateRange.class, dateRangeMapper);
    dbi.registerColumnMapper(Title.class, titleMapper);

    this.entityRepository = entityRepository;
    this.agentRepository = agentRepository;
    this.humanSettlementRepository = humanSettlementRepository;
  }

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

  protected static void additionalReduceRowsBiConsumer(
      Map<UUID, Manifestation> map, RowView rowView) {
    Manifestation manifestation = map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));
    // This object should exist already. If not, the mistake is somewhere in IdentifiableRepo.

    // subjects
    UUID subjectUuid =
        rowView.getColumn(SubjectRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class);
    if (subjectUuid != null
        && (manifestation.getSubjects() == null
            || !manifestation.getSubjects().stream()
                .anyMatch(subj -> Objects.equals(subj.getUuid(), subjectUuid)))) {
      Subject subject = rowView.getRow(Subject.class);
      manifestation.addSubject(subject);
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

  private BiFunction<String, Map<String, Object>, String> buildTitleSql(
      final Manifestation manifestation) {
    return (sql, bindings) -> {
      if (manifestation.getTitles() == null || manifestation.getTitles().isEmpty()) {
        return sql.replace("{{titles}}", "NULL");
      }
      List<String> titleConstructors = new ArrayList<>();
      for (int i = 0; i < manifestation.getTitles().size(); i++) {
        titleConstructors.add(
            String.format(
                "title_constructor(:titles_%1$d_mainType, :titles_%1$d_subType, "
                    + ":titles_%1$d_text::jsonb, :titles_%1$d_textLocales::varchar[])",
                i));
        Title title = manifestation.getTitles().get(i);
        bindings.put(
            String.format("titles_%d_mainType", i),
            title.getTitleType() != null ? title.getTitleType().getMainType() : null);
        bindings.put(
            String.format("titles_%d_subType", i),
            title.getTitleType() != null ? title.getTitleType().getSubType() : null);
        bindings.put(String.format("titles_%d_text", i), title.getText());
        bindings.put(
            String.format("titles_%d_textLocales", i),
            title.getTextLocalesOfOriginalScripts() != null
                ? title.getTextLocalesOfOriginalScripts().stream()
                    .map(l -> l.toLanguageTag())
                    .toArray(n -> new String[n])
                : null);
      }
      return sql.replace("{{titles}}", "ARRAY[" + String.join(", ", titleConstructors) + "]");
    };
  }

  @Override
  protected void extendReducedIdentifiable(Manifestation manifestation, RowView rowView) {
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
              EntityRelationRepositoryImpl.MAPPING_PREFIX + "_predicate", String.class);
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
                    EntityRelationRepositoryImpl.MAPPING_PREFIX + "_sortindex", Integer.class),
                EntityRelation.builder()
                    .subject(relatedEntity)
                    .predicate(relationPredicate)
                    .additionalPredicates(
                        rowView.getColumn(
                            EntityRelationRepositoryImpl.MAPPING_PREFIX + "_additionalPredicates",
                            new GenericType<List<String>>() {}))
                    .build());
      }
    }
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
    List<Publisher> publishers =
        publishingInfo.getPublishers().stream()
            .filter(publ -> publ != null)
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

  private void saveParents(Manifestation manifestation) {
    if (manifestation == null) return;
    /* - subject (subject_uuid) is the parent (superior manifestation)
     * - object (object_uuid) is the child, i.e. this manifestation parameter
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

  @Override
  public void save(Manifestation manifestation, Map<String, Object> bindings)
      throws RepositoryException {
    if (bindings == null) {
      bindings = new HashMap<>(2);
    }
    bindings.put("subjects_uuids", extractUuids(manifestation.getSubjects()));

    DistributionInfo distributionInfo = manifestation.getDistributionInfo();
    manifestation.setDistributionInfo(reducePublisher(distributionInfo));
    ProductionInfo productionInfo = manifestation.getProductionInfo();
    manifestation.setProductionInfo(reducePublisher(productionInfo));
    PublicationInfo publicationInfo = manifestation.getPublicationInfo();
    manifestation.setPublicationInfo(reducePublisher(publicationInfo));

    super.save(manifestation, bindings, buildTitleSql(manifestation));
    saveParents(manifestation);

    manifestation.setDistributionInfo(distributionInfo);
    manifestation.setProductionInfo(productionInfo);
    manifestation.setPublicationInfo(publicationInfo);
  }

  @Override
  public void update(Manifestation manifestation, Map<String, Object> bindings)
      throws RepositoryException {
    if (bindings == null) {
      bindings = new HashMap<>(2);
    }
    bindings.put("subjects_uuids", extractUuids(manifestation.getSubjects()));

    DistributionInfo distributionInfo = manifestation.getDistributionInfo();
    manifestation.setDistributionInfo(reducePublisher(distributionInfo));
    ProductionInfo productionInfo = manifestation.getProductionInfo();
    manifestation.setProductionInfo(reducePublisher(productionInfo));
    PublicationInfo publicationInfo = manifestation.getPublicationInfo();
    manifestation.setPublicationInfo(reducePublisher(publicationInfo));

    super.update(manifestation, bindings, buildTitleSql(manifestation));
    saveParents(manifestation);

    manifestation.setDistributionInfo(distributionInfo);
    manifestation.setProductionInfo(productionInfo);
    manifestation.setPublicationInfo(publicationInfo);
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
  protected List<String> getAllowedOrderByFields() {
    List<String> orderByFields = super.getAllowedOrderByFields();
    return orderByFields;
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
}
