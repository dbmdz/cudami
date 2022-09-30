package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.work;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.work.ManifestationRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.EntityRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.MainSubTypeMapper.ExpressionTypeMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.PublicationMapper;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.TitleMapper;
import de.digitalcollections.model.identifiable.entity.work.ExpressionType;
import de.digitalcollections.model.identifiable.entity.work.Manifestation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
   *   - DateRange (??)
   *
   * - remove involvements trigger
   */

  public static final String TABLE_NAME = "manifestations";
  public static final String TABLE_ALIAS = "mf";
  public static final String MAPPING_PREFIX = "mf";

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", composition, dimensions, expressiontypes"
        + ", language, manufacturingtype"
        + ", mediatypes, otherlanguages, parent"
        + ", publications, publishingdatepresentation, publishingdaterange"
        + ", scale, series_uuids, sortkey"
        + ", subjects_uuids, titles, version"
        + ", work";
  }

  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :composition, :dimensions, :expressionTypes::mainsubtype[]"
        + ", :language, :manufacturingType"
        + ", :mediaTypes::varchar[], :otherLanguages::varchar[], :parent?.uuid"
        + ", :publications::publication[], :publishingDatePresentation, :publishingDateRange::daterange"
        + ", :scale, :series_uuids::UUID[], :sortKey"
        + ", :subjects_uuids::UUID[], :titles::title[], :version"
        + ", :work?.uuid";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + String.format(
            ", %1$s.composition %2$s_composition, %1$s.dimensions %2$_dimensions, %1$s.otherlanguages %2$s_otherLanguages"
                + ", %1$s.publications %2$s_publications, %1$s.publishingdatepresentation %2$s_publishingDatePresentation, %1$s.publishingdaterange %2$s_publishingDateRange"
                + ", %1$s.scale %2$s_scale, %1$s.sortkey %2$s_sortKey, %1$s.version %2$s_version",
            tableAlias, mappingPrefix);
  }

  // to join: series, subjects, parent, work

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + String.format(
            ", %1$s.expressiontypes %2$s_expressionTypes, %1$s.manufacturingtype %2$s_manufacturingType"
                + ", %1$s.language %2$s_language, %1$s.mediatypes %2$s_mediaTypes, %1$s.parent %2$s_parent"
                + ", %1$s.titles %2$s_titles, %1$s.work %2$s_work",
            tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + "composition=:composition, dimensions=:dimensions, expressiontypes=:expressionTypes::mainsubtype[], "
        + "language=:language, manufacturingtype=:manufacturingType, "
        + "mediatypes=:mediaTypes::varchar[], otherlanguages=:otherLanguages::varchar[], parent=:parent?.uuid, "
        + "publications=:publications::publication[], publishingdatepresentation=:publishingDatePresentation, publishingdaterange=:publishingDateRange::daterange, "
        + "scale=:scale, series_uuids=:series_uuids::UUID[], sortkey=:sortKey, "
        + "subjects_uuids=:subjects_uuids::UUID[], titles=:titles::title[], version=:version, "
        + "work=:work?.uuid";
  }

  public ManifestationRepositoryImpl(
      Jdbi jdbi,
      CudamiConfig cudamiConfig,
      ExpressionTypeMapper expressionTypeMapper,
      PublicationMapper publicationMapper,
      TitleMapper titleMapper) {
    super(
        jdbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Manifestation.class,
        cudamiConfig.getOffsetForAlternativePaging());
    dbi.registerArrayType(expressionTypeMapper);
    dbi.registerArrayType(publicationMapper);
    dbi.registerArrayType(titleMapper);
    dbi.registerColumnMapper(ExpressionType.class, expressionTypeMapper);
  }

  @Override
  public Manifestation save(Manifestation manifestation, Map<String, Object> bindings) {
    if (bindings == null) {
      bindings = new HashMap<>(2);
    }
    bindings.put("series_uuids", extractUuids(manifestation.getSeries()));
    bindings.put("subjects_uuids", extractUuids(manifestation.getSubjects()));
    return super.save(manifestation, bindings);
  }

  @Override
  public Manifestation update(Manifestation manifestation, Map<String, Object> bindings) {
    if (bindings == null) {
      bindings = new HashMap<>(2);
    }
    bindings.put("series_uuids", extractUuids(manifestation.getSeries()));
    bindings.put("subjects_uuids", extractUuids(manifestation.getSubjects()));
    return super.update(manifestation, bindings);
  }

  @Override
  public String getColumnName(String modelProperty) {
    switch (modelProperty) {
      case "composition":
      case "dimensions":
      case "language":
      case "parent":
      case "publications":
      case "scale":
      case "titles":
      case "version":
      case "work":
        return modelProperty;
      case "expressionTypes":
      case "manufacturingType":
      case "mediaTypes":
      case "otherLanguages":
      case "publishingDatePresentation":
      case "publishingDateRange":
      case "sortKey":
        return modelProperty.toLowerCase();
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> orderByFields = super.getAllowedOrderByFields();
    orderByFields.add("sortKey");
    return orderByFields;
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "composition":
      case "dimensions":
      case "language":
      case "manufacturingType":
      case "publishingDatePresentation":
      case "scale":
      case "sortKey":
      case "version":
        return true;
      default:
        return super.supportsCaseSensitivityForProperty(modelProperty);
    }
  }
}
