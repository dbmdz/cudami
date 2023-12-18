package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.DbIdentifierMapper;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;

@Repository
public class SubjectRepositoryImpl extends UniqueObjectRepositoryImpl<Subject>
    implements SubjectRepository {

  public static final String MAPPING_PREFIX = "subj";
  public static final String TABLE_ALIAS = "subj";
  public static final String TABLE_NAME = "subjects";

  public static String sqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return sqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String sqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return UniqueObjectRepositoryImpl.sqlSelectReducedFields(tableAlias, mappingPrefix)
        + """
       , dbidentifiers2jsonb({{tableAlias}}.identifiers) {{mappingPrefix}}_identifiers,
       {{tableAlias}}.label {{mappingPrefix}}_label,
       {{tableAlias}}.type {{mappingPrefix}}_subjectType
       """
            .replace("{{tableAlias}}", tableAlias)
            .replace("{{mappingPrefix}}", mappingPrefix);
  }

  public SubjectRepositoryImpl(
      Jdbi dbi, CudamiConfig cudamiConfig, DbIdentifierMapper dbIdentifierMapper) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Subject.class,
        cudamiConfig.getOffsetForAlternativePaging());

    dbi.registerRowMapper(BeanMapper.factory(Subject.class, MAPPING_PREFIX));
    this.dbi.registerArrayType(dbIdentifierMapper);
  }

  @Override
  public Subject create() throws RepositoryException {
    return new Subject();
  }

  @Override
  public PageResponse<Subject> find(PageRequest pageRequest) throws RepositoryException {
    Map argumentMappings = new HashMap<>(0);
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " left join unnest(subj.identifiers) as "
            + tableAlias
            + "_identifier on true";
    StringBuilder commonSqlBuilder = new StringBuilder(commonSql);
    addFiltering(pageRequest, commonSqlBuilder, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".* " + commonSqlBuilder);
    addPagingAndSorting(pageRequest, innerQuery);
    List<Subject> result =
        retrieveList(
            getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            getOrderBy(pageRequest.getSorting()));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSqlBuilder);
    long total = retrieveCount(countQuery, argumentMappings);

    PageResponse<Subject> pageResponse = new PageResponse<>(result, pageRequest, total);

    // FIXME: try to avoid doing this after database select! Delete when jsonb
    // search without split-field implemented
    filterByLocalizedTextFields(pageRequest, pageResponse, getJsonbFields());

    return pageResponse;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("label", "subjectType"));
    return allowedOrderByFields;
  }

  @Override
  public Subject getByTypeAndIdentifier(String subjectType, String namespace, String id)
      throws RepositoryException {
    final String sql =
        "SELECT "
            + getSqlSelectAllFields()
            + " FROM "
            + tableName
            + " "
            + tableAlias
            + " left join UNNEST("
            + tableAlias
            + ".identifiers) subjids on true "
            + String.format(" WHERE %s.type = :subjectType", tableAlias)
            + " AND subjids.namespace = :namespace"
            + " AND subjids.id = :id";

    return dbi.withHandle(
        h ->
            h.createQuery(sql)
                .bind("subjectType", subjectType)
                .bind("namespace", namespace)
                .bind("id", id)
                .mapTo(Subject.class)
                .findOne()
                .orElse(null));
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "label":
        return tableAlias + ".label";
      case "subjectType":
        return tableAlias + ".type";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  protected LinkedHashMap<String, Function<Subject, Optional<Object>>> getJsonbFields() {
    LinkedHashMap<String, Function<Subject, Optional<Object>>> linkedHashMap =
        super.getJsonbFields();
    linkedHashMap.put("label", i -> Optional.ofNullable(i.getLabel()));
    return linkedHashMap;
  }

  // TODO: duplicate code to IdentifiableRepositoryImpl as we do not inherit...
  @Override
  public List<Locale> getLanguages() throws RepositoryException {
    String query =
        "SELECT DISTINCT jsonb_object_keys("
            + tableAlias
            + ".label) as languages FROM "
            + tableName
            + " AS "
            + tableAlias;
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public List<Subject> getRandom(int count) throws RepositoryException {
    throw new UnsupportedOperationException(); // TODO: not yet implemented
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", identifiers, label, split_label, type";
  }

  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :identifiers, :label::JSONB, :split_label, :subjectType";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return sqlSelectAllFields(tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return sqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    // do not update/left out from statement (not changed since insert):
    // uuid, created
    return super.getSqlUpdateFieldValues()
        + ", identifiers=:identifiers, label=:label::JSONB, split_label=:split_label, type=:subjectType";
    // TODO?: in IdentifiableRepoImpl it is "split_label=:split_label::TEXT[]"...?
  }

  @Override
  protected String getTargetExpression(FilterCriterion<?> fc) throws IllegalArgumentException {
    String givenExpression = fc.getExpression(); // e.g. "identifier.namespace"
    if (fc.isNativeExpression()) {
      return getTableAlias() + "_" + givenExpression;
    } else {
      return super.getTargetExpression(fc);
    }
  }

  @Override
  protected boolean hasSplitColumn(String propertyName) {
    // only label for now
    return switch (propertyName) {
      case "label" -> true;
      default -> super.hasSplitColumn(propertyName);
    };
  }

  @Override
  public void save(Subject subject) throws RepositoryException, ValidationException {
    HashMap<String, Object> bindings = new HashMap<>(0);
    bindings.put("split_label", splitToArray(subject.getLabel()));
    super.save(subject, bindings);
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    if (modelProperty == null) {
      return false;
    }

    switch (modelProperty) {
      case "identifiers_namespace":
      case "subjectType":
        return true;
      default:
        return false;
    }
  }

  @Override
  public void update(Subject subject) throws RepositoryException, ValidationException {
    HashMap<String, Object> bindings = new HashMap<>(0);
    bindings.put("split_label", splitToArray(subject.getLabel()));
    super.update(subject, bindings);
  }
}
