package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.WorkRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.AgentRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation.EntityRelationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.semantic.SubjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.LocalDateRangeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.MainSubTypeMapper.ExpressionTypeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.TitleMapper;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.agent.Agent;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.agent.Family;
import de.digitalcollections.model.identifiable.entity.agent.Person;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.manifestation.ExpressionType;
import de.digitalcollections.model.identifiable.entity.work.Work;
import de.digitalcollections.model.semantic.Subject;
import de.digitalcollections.model.text.Title;
import de.digitalcollections.model.time.LocalDateRange;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import org.jdbi.v3.core.Jdbi;
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

  private EntityRepositoryImpl<Entity> entityRepository;
  private AgentRepositoryImpl<Agent> agentRepository;
  private HumanSettlementRepositoryImpl humanSettlementRepository;

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", creation_daterange"
        + ", creation_timevalue"
        + ", first_appeared_date"
        + ", first_appeared_presentation"
        + ", first_appeared_timevalue"
        + ", subjects_uuids";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :creationDateRange::JSONB"
        + ", :creationTimeValue::JSONB"
        + ", :firstAppearedDate"
        + ", :firstAppearedDatePresentation"
        + ", :firstAppearedTimeValue::JSONB"
        + ", :subjects_uuids::UUID[]";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
            , %1$s.creation_daterange %2$s_creationDateRange
            , %1$s.first_appeared_date %2$s_firstAppearedDate
            , %1$s.first_appeared_presentation %2$s_firstAppearedDatePresentation
            , %1$s.titles %2$s_titles,
            """
            .formatted(tableAlias, mappingPrefix)
        // parents
        + """
            parent.uuid parent_uuid, parent.label parent_label, parent.titles parent_titles,
            parent.refid parent_refId, parent.notes parent_notes, parent.created parent_created, parent.last_modified parent_lastModified,
            parent.identifiable_objecttype parent_identifiableObjectType,
            """
        // relations
        + """
          %1$s.predicate %2$s_predicate, %1$s.sortindex %2$s_sortindex,
          %1$s.additional_predicates %2$s_additionalPredicates,
          max(%1$s.sortindex) OVER (PARTITION BY %3$s.uuid) relation_max_sortindex,
          """
            .formatted(
                EntityRelationRepositoryImpl.TABLE_ALIAS,
                EntityRelationRepositoryImpl.MAPPING_PREFIX,
                tableAlias)
        + entityRepository.getSqlSelectReducedFields();
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", creation_daterange=:creationDateRange::JSONB"
        + ", creation_timevalue=:creationTimeValue::JSONB"
        + ", first_appeared_date=:firstAppearedDate"
        + ", first_appeared_presentation=:firstAppearedDatePresentation"
        + ", first_appeared_timevalue=:firstAppearedTimeValue::JSONB"
        + ", subjects_uuids=:subjects_uuids::UUID[]";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        // subjects
        + SubjectRepositoryImpl.SQL_REDUCED_FIELDS_SUBJECTS
        // publishers
        + ", %s, %s"
            .formatted(
                agentRepository.getSqlSelectReducedFields(),
                humanSettlementRepository.getSqlSelectReducedFields());
  }

  public static final String SQL_SELECT_ALL_FIELDS_JOINS =
      """
      LEFT JOIN %2$s %3$s ON %3$s.uuid = ANY (%1$s.subjects_uuids)
      LEFT JOIN (
        work_works wws INNER JOIN works parent
        ON parent.uuid = wws.subject_uuid
      ) ON wws.object_uuid = %1$s.uuid
      LEFT JOIN (
        %4$s %5$s INNER JOIN %6$s %7$s ON %5$s.subject_uuid = %7$s.uuid
      ) ON %5$s.object_uuid = %1$s.uuid
      """
          .formatted(
              TABLE_ALIAS,
              /*2-3*/ SubjectRepositoryImpl.TABLE_NAME,
              SubjectRepositoryImpl.TABLE_ALIAS,
              /*4-5*/ EntityRelationRepositoryImpl.TABLE_NAME,
              EntityRelationRepositoryImpl.TABLE_ALIAS,
              /*6-7*/ EntityRepositoryImpl.TABLE_NAME,
              EntityRepositoryImpl.TABLE_ALIAS);

  public WorkRepositoryImpl(
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
        Work.class,
        SQL_SELECT_ALL_FIELDS_JOINS,
        WorkRepositoryImpl::additionalReduceRowsBiConsumer,
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

  @Override
  public Set<Work> getWorksForItem(UUID itemUuid) {
    // FIXME
    return null;
  }

  @Override
  public void save(Work work, Map<String, Object> bindings) throws RepositoryException {
    if (bindings == null) {
      bindings = new HashMap<>(3);
    }
    bindings.put("subjects_uuids", extractUuids(work.getSubjects()));
    super.save(work, bindings, buildTitleSql(work));
    saveParents(work);
  }

  @Override
  public void update(Work work, Map<String, Object> bindings) throws RepositoryException {
    if (bindings == null) {
      bindings = new HashMap<>(3);
    }
    bindings.put("subjects_uuids", extractUuids(work.getSubjects()));

    super.update(work, bindings, buildTitleSql(work));
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

  private BiFunction<String, Map<String, Object>, String> buildTitleSql(final Work work) {
    return (sql, bindings) -> {
      if (work.getTitles() == null || work.getTitles().isEmpty()) {
        return sql.replace("{{titles}}", "NULL");
      }
      List<String> titleConstructors = new ArrayList<>();
      for (int i = 0; i < work.getTitles().size(); i++) {
        titleConstructors.add(
            String.format(
                "title_constructor(:titles_%1$d_mainType, :titles_%1$d_subType, "
                    + ":titles_%1$d_text::jsonb, :titles_%1$d_textLocales::varchar[])",
                i));
        Title title = work.getTitles().get(i);
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

  protected static void additionalReduceRowsBiConsumer(Map<UUID, Work> map, RowView rowView) {
    Work work = map.get(rowView.getColumn(MAPPING_PREFIX + "_uuid", UUID.class));
    // This object should exist already. If not, the mistake is somewhere in IdentifiableRepo.

    // subjects
    UUID subjectUuid =
        rowView.getColumn(SubjectRepositoryImpl.MAPPING_PREFIX + "_uuid", UUID.class);
    if (subjectUuid != null
        && (work.getSubjects() == null
            || !work.getSubjects().stream()
                .anyMatch(subj -> Objects.equals(subj.getUuid(), subjectUuid)))) {
      Subject subject = rowView.getRow(Subject.class);
      work.addSubject(subject);
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
  }
}
