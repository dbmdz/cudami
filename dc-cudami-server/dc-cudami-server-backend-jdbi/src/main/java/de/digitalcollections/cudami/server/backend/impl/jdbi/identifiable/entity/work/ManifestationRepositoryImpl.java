package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.relation.EntityRelationRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.semantic.SubjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.LocalDateRangeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.MainSubTypeMapper.ExpressionTypeMapper;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.identifiable.entity.work.Manifestation;
import de.digitalcollections.model.identifiable.entity.work.Title;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class ManifestationRepositoryImpl extends EntityRepositoryImpl<Manifestation>
    implements ManifestationRepository {

  /* THIS IS STILL A BIG TODO!
   *
   * - ArgumentMapper for
   *
   *   - Publication ✓
   *   - Title ✓
   *   - MainSubType ✓
   *   - DateRange ✓
   *
   * - remove involvements trigger ✓
   */

  public static final String TABLE_NAME = "manifestations";
  public static final String TABLE_ALIAS = "mf";
  public static final String MAPPING_PREFIX = "mf";

  private EntityRepositoryImpl<Entity> entityRepository;

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", composition, dimensions, expressiontypes"
        + ", language, manifestationtype, manufacturingtype"
        + ", mediatypes, otherlanguages"
        + ", publishingdatepresentation, publishingdaterange, publishing_timevaluerange"
        + ", scale, subjects_uuids, version"
        + ", work, titles";
  }

  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :composition, :dimensions, :expressionTypes::mainsubtype[]"
        + ", :language, :manifestationType, :manufacturingType"
        + ", :mediaTypes::varchar[], :otherLanguages::varchar[]"
        + ", :publishingDatePresentation, :publishingDateRange::daterange, :publishingTimeValueRange::jsonb"
        + ", :scale, :subjects_uuids::UUID[], :version"
        + ", :work?.uuid, {{titles}}";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + "composition=:composition, dimensions=:dimensions, expressiontypes=:expressionTypes::mainsubtype[], "
        + "language=:language, manifestationtype=:manifestationType, manufacturingtype=:manufacturingType, "
        + "mediatypes=:mediaTypes::varchar[], otherlanguages=:otherLanguages::varchar[], "
        + "publishingdatepresentation=:publishingDatePresentation, publishingdaterange=:publishingDateRange::daterange, "
        + "publishing_timevaluerange=:publishingTimeValueRange::jsonb, scale=:scale, "
        + "subjects_uuids=:subjects_uuids::UUID[], version=:version, "
        + "work=:work?.uuid, titles={{titles}}";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + String.format(
            """
            , %1$s.composition %2$s_composition, %1$s.dimensions %2$s_dimensions, %1$s.otherlanguages %2$s_otherLanguages,
            %1$s.publishingdatepresentation %2$s_publishingDatePresentation, %1$s.publishingdaterange %2$s_publishingDateRange,
            %1$s.publishing_timevaluerange %2$s_publishingTimeValueRange, %1$s.scale %2$s_scale, %1$s.version %2$s_version, """,
            tableAlias, mappingPrefix)
        + SubjectRepositoryImpl.SQL_REDUCED_FIELDS_SUBJECTS;
  }

  // TODO: to join: AllFields{ subjects ✓ }, ReducedFields{ work, parents ✓ and relations ✓ }

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
            """
        // relations
        + "%1$s.predicate %2$s_predicate, %1$s.sortKey %2$s_sortKey, "
            .formatted(
                EntityRelationRepositoryImpl.TABLE_ALIAS,
                EntityRelationRepositoryImpl.MAPPING_PREFIX)
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
      """
          .formatted(
              TABLE_ALIAS,
              /*2-3*/ SubjectRepositoryImpl.TABLE_NAME,
              SubjectRepositoryImpl.TABLE_ALIAS,
              /*4-5*/ EntityRelationRepositoryImpl.TABLE_NAME,
              EntityRelationRepositoryImpl.TABLE_ALIAS,
              /*6-7*/ EntityRepositoryImpl.TABLE_NAME,
              EntityRepositoryImpl.TABLE_ALIAS);

  public ManifestationRepositoryImpl(
      Jdbi jdbi,
      CudamiConfig cudamiConfig,
      ExpressionTypeMapper expressionTypeMapper,
      LocalDateRangeMapper dateRangeMapper,
      EntityRepositoryImpl<Entity> entityRepository) {
    super(
        jdbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Manifestation.class,
        SQL_SELECT_ALL_FIELDS_JOINS,
        cudamiConfig.getOffsetForAlternativePaging());
    dbi.registerArrayType(expressionTypeMapper);
    dbi.registerArgument(dateRangeMapper);
    dbi.registerColumnMapper(ExpressionType.class, expressionTypeMapper);
    dbi.registerColumnMapper(dateRangeMapper);

    this.entityRepository = entityRepository;
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
            String.format("titles_%d_textLocales", i), title.getTextLocalesOfOriginalScripts());
      }
      return sql.replace("{{titles}}", "ARRAY[" + String.join(", ", titleConstructors) + "]");
    };
  }

  @Override
  public Manifestation save(Manifestation manifestation, Map<String, Object> bindings) {
    if (bindings == null) {
      bindings = new HashMap<>(2);
    }
    bindings.put("subjects_uuids", extractUuids(manifestation.getSubjects()));
    return super.save(manifestation, bindings, buildTitleSql(manifestation));
  }

  @Override
  public Manifestation update(Manifestation manifestation, Map<String, Object> bindings) {
    if (bindings == null) {
      bindings = new HashMap<>(2);
    }
    bindings.put("subjects_uuids", extractUuids(manifestation.getSubjects()));
    return super.update(manifestation, bindings, buildTitleSql(manifestation));
  }

  @Override
  public String getColumnName(String modelProperty) {
    switch (modelProperty) {
      case "composition":
      case "dimensions":
      case "language":
      case "publications":
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
      case "publishingDatePresentation":
      case "publishingDateRange":
        return modelProperty.toLowerCase();
      case "publishingTimeValueRange":
        return "publishing_timevaluerange";
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
