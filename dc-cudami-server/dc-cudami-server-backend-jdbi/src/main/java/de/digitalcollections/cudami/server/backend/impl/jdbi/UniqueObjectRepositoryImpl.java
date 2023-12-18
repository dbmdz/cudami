package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.validation.ValidationException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.StatementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public abstract class UniqueObjectRepositoryImpl<U extends UniqueObject>
    extends JdbiRepositoryImpl<U> implements UniqueObjectRepository<U> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UniqueObjectRepositoryImpl.class);

  public static String sqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return " "
        + """
        {{alias}}.uuid {{prefix}}_uuid, {{alias}}.created {{prefix}}_created, {{alias}}.last_modified {{prefix}}_lastModified"""
            .replace("{{alias}}", tableAlias)
            .replace("{{prefix}}", mappingPrefix);
  }

  protected final Class<? extends UniqueObject> uniqueObjectImplClass;

  protected UniqueObjectRepositoryImpl() {
    super();
    this.uniqueObjectImplClass = null;
  }

  protected UniqueObjectRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends UniqueObject> uniqueObjectImplClass,
      int offsetForAlternativePaging) {
    super(dbi, tableName, tableAlias, mappingPrefix, offsetForAlternativePaging);

    this.dbi.registerRowMapper(BeanMapper.factory(uniqueObjectImplClass, mappingPrefix));
    this.uniqueObjectImplClass = uniqueObjectImplClass;
  }

  /**
   * For details select (only used in find one, not lists): if additional objects should be "joined"
   */
  protected void additionalReduceRowsBiConsumer(Map<UUID, U> map, RowView rowView) {}

  /** The basic reduce rows biconsumer for reduced selects (lists, paging) */
  @SuppressWarnings("unchecked")
  protected void basicReduceRowsBiConsumer(Map<UUID, U> map, RowView rowView) {
    map.computeIfAbsent(
        rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
        uuid -> (U) rowView.getRow(uniqueObjectImplClass));
  }

  /**
   * The full reduce rows biconsumer for full selects (find one).<br>
   * This method calls {@link #basicReduceRowsBiConsumer(Map, RowView)}, so make sure to call {@code
   * super} first and only implement extraneous object building.<br>
   * For a plain UniqueObject full and basic are the same (minimal dataset of uuid, lastModified,
   * created).
   */
  protected void fullReduceRowsBiConsumer(Map<UUID, U> map, RowView rowView) {
    basicReduceRowsBiConsumer(map, rowView);
  }

  @Override
  public boolean deleteByUuid(UUID uuid) throws RepositoryException {
    // same performance as delete by where uuid = :uuid
    return deleteByUuids(List.of(uuid)) > 0 ? true : false;
  }

  @Override
  public int deleteByUuids(List<UUID> uuids) throws RepositoryException {
    final String sql = "DELETE FROM " + tableName + " WHERE uuid in (<uuids>)";
    return execUpdateWithList(sql, "uuids", uuids);
  }

  protected boolean isConstraintViolationException(
      Throwable throwable, Consumer<String> useMessage) {
    if (throwable == null) return false;
    if (throwable instanceof SQLException sqlexc) {
      useMessage.accept(sqlexc.getMessage());
      return List.of("foreign_key_violation", "unique_violation", "check_violation")
          .contains(sqlexc.getSQLState());
    }
    return throwable.getCause() != null
        ? isConstraintViolationException(throwable.getCause(), useMessage)
        : false;
  }

  private void execInsertUpdate(
      final String sql, U uniqueObject, final Map<String, Object> bindings, boolean withCallback)
      throws RepositoryException, ValidationException {
    // because of a significant difference in execution duration it makes sense to
    // distinguish here
    try {
      if (withCallback) {
        Map<String, Object> returnedFields =
            dbi.withHandle(
                h ->
                    h.createQuery(sql)
                        .bindMap(bindings)
                        .bindBean(uniqueObject)
                        .mapToMap()
                        .findOne()
                        .orElse(Collections.emptyMap()));
        insertUpdateCallback(uniqueObject, returnedFields);
      } else {
        int affected =
            dbi.withHandle(
                h -> h.createUpdate(sql).bindMap(bindings).bindBean(uniqueObject).execute());
        if (affected != 1) {
          throw new RepositoryException(
              "Insert into table " + getTableName() + " failed for %s".formatted(uniqueObject));
        }
      }
    } catch (StatementException e) {
      AtomicReference<String> constraintMessage = new AtomicReference<>();
      if (isConstraintViolationException(e, constraintMessage::set)) {
        throw new ValidationException(constraintMessage.get());
      }
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  protected List<U> execSelectForList(final String sql, final Map<String, Object> bindings)
      throws RepositoryException {
    try {
      if (bindings == null)
        return dbi.withHandle(
            (Handle handle) ->
                handle
                    .createQuery(sql)
                    .reduceRows(this::basicReduceRowsBiConsumer)
                    .collect(Collectors.toList()));

      return dbi.withHandle(
          (Handle handle) ->
              handle
                  .createQuery(sql)
                  .bindMap(bindings)
                  .reduceRows(this::basicReduceRowsBiConsumer)
                  .collect(Collectors.toList()));
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  protected UUID[] extractUuids(Collection<? extends UniqueObject> uniqueObjects) {
    if (uniqueObjects == null || uniqueObjects.isEmpty()) {
      return new UUID[0];
    }
    return uniqueObjects.stream()
        .collect(
            ArrayList<UUID>::new,
            (result, uniqueObject) -> result.add(uniqueObject.getUuid()),
            ArrayList::addAll)
        .toArray(new UUID[1]);
  }

  @Override
  public PageResponse<U> find(PageRequest pageRequest) throws RepositoryException {
    return find(pageRequest, (Map<String, Object>) null);
  }

  protected PageResponse<U> find(PageRequest pageRequest, Map<String, Object> argumentMappings)
      throws RepositoryException {
    String commonSql = " FROM " + tableName + " AS " + tableAlias;
    return find(pageRequest, commonSql, argumentMappings);
  }

  protected PageResponse<U> find(PageRequest pageRequest, String commonSql)
      throws RepositoryException {
    return find(pageRequest, commonSql, null);
  }

  protected PageResponse<U> find(
      PageRequest pageRequest, String commonSql, Map<String, Object> argumentMappings)
      throws RepositoryException {
    if (argumentMappings == null) {
      argumentMappings = new HashMap<>(0);
    }
    StringBuilder commonSqlBuilder = new StringBuilder(commonSql);
    addFiltering(pageRequest, commonSqlBuilder, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".* " + commonSqlBuilder);
    addPagingAndSorting(pageRequest, innerQuery);
    List<U> result =
        retrieveList(
            getSqlSelectReducedFields(),
            innerQuery,
            argumentMappings,
            getOrderBy(pageRequest.getSorting()));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSqlBuilder);
    long total = retrieveCount(countQuery, argumentMappings);

    PageResponse<U> pageResponse = new PageResponse<>(result, pageRequest, total);

    return pageResponse;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("created", "lastModified"));
  }

  @Override
  public List<U> getByUuidsAndFiltering(List<UUID> uuids, Filtering filtering)
      throws RepositoryException {
    if (filtering == null) {
      filtering = new Filtering();
    }
    filtering.add(FilterCriterion.builder().withExpression("uuid").in(uuids).build());

    List<U> result = retrieveMultiple(getSqlSelectAllFields(), filtering, null);
    return result;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "uuid":
        return tableAlias + ".uuid";
      default:
        return null;
    }
  }

  public int getIndex(List<? extends UniqueObject> list, UniqueObject uniqueObject) {
    int pos = -1;
    for (UniqueObject uo : list) {
      pos += 1;
      if (uo.getUuid().equals(uniqueObject.getUuid())) {
        return pos;
      }
    }
    return -1;
  }

  /**
   * On insert or update these fields will be returned to be processed by {@link
   * #insertUpdateCallback(UniqueObject, Map)}.
   *
   * @return modifiable list of fields, please do not return null
   */
  protected List<String> getReturnedFieldsOnInsertUpdate() {
    return new ArrayList<>(0);
  }

  /**
   * @return SQL for field names for insert statement of {@code UniqueObject}
   */
  protected String getSqlInsertFields() {
    return "uuid, created, last_modified";
  }

  /**
   * Do not change order! Must match order in getSqlInsertFields!!!
   *
   * @return SQL for value mapping for field names for insert statement of {@code UniqueObject}
   */
  protected String getSqlInsertValues() {
    return ":uuid, :created, :lastModified";
  }

  /**
   * SQL-snippet for fields to be returned for complete field request.<br>
   * If already all fields are returned with reduced fields request: just return reduced field set
   * here, otherwise add additional fields to reduced set to get all fields.
   *
   * @return SQL snippet
   */
  public String getSqlSelectAllFields() {
    return getSqlSelectAllFields(tableAlias, mappingPrefix);
  }

  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  /**
   * @return SQL for joined fields for complete field set from other table(s) (optional). Default:
   *     ""
   */
  protected String getSqlSelectAllFieldsJoins() {
    return "";
  }

  public String getSqlSelectReducedFields() {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return sqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  /**
   * @return SQL for joined fields for reduced field set from other table(s) (optional). Default: ""
   */
  protected String getSqlSelectReducedFieldsJoins() {
    return "";
  }

  protected String getSqlUpdateFieldValues() {
    // do not update/left out from statement (not changed since insert):
    // uuid, created
    return " last_modified=:lastModified";
  }

  @Override
  protected String getUniqueField() {
    return "uuid";
  }

  /**
   * After save and update the returned fields (declared in {@link
   * #getReturnedFieldsOnInsertUpdate()}) can be processed here.
   *
   * @param uniqueObject the object that was passed to save/update
   * @param returnedFields returned fields in a map of column names to values
   */
  protected void insertUpdateCallback(U uniqueObject, Map<String, Object> returnedFields) {
    // can be implemented in derived classes
  }

  @Override
  public long retrieveCount(StringBuilder sqlCount, final Map<String, Object> argumentMappings)
      throws RepositoryException {
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(sqlCount.toString())
                    .bindMap(argumentMappings)
                    .mapTo(Long.class)
                    .findOne()
                    .get());
    return total;
  }

  protected List<U> retrieveList(
      String fieldsSql,
      String fieldsSqlAdditionalJoins,
      StringBuilder innerQuery,
      final Map<String, Object> argumentMappings,
      String orderBy)
      throws RepositoryException {
    final String sql =
        "SELECT "
            + fieldsSql
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (StringUtils.hasText(fieldsSqlAdditionalJoins)
                ? " %s".formatted(fieldsSqlAdditionalJoins)
                : "")
            + (StringUtils.hasText(getSqlSelectReducedFieldsJoins())
                ? " %s ".formatted(getSqlSelectReducedFieldsJoins())
                : "")
            + (orderBy != null && orderBy.matches("(?iu)^\\s*order by.+")
                ? " " + orderBy
                : (StringUtils.hasText(orderBy) ? " ORDER BY " + orderBy : ""));

    // handle.execute("SET cust.code=:customerID", "bav");
    // multitenancy, see
    // https://varun-verma.medium.com/isolate-multi-tenant-data-in-postgresql-db-using-row-level-security-rls-bdd3089d9337
    // https://aws.amazon.com/de/blogs/database/multi-tenant-data-isolation-with-postgresql-row-level-security/
    // https://www.postgresql.org/docs/current/ddl-rowsecurity.html
    // https://www.postgresql.org/docs/current/sql-createpolicy.html
    List<U> result = execSelectForList(sql, argumentMappings);
    return result;
  }

  public List<U> retrieveList(
      String fieldsSql,
      StringBuilder innerQuery,
      final Map<String, Object> argumentMappings,
      String orderBy)
      throws RepositoryException {
    // no additional joins
    return retrieveList(fieldsSql, null, innerQuery, argumentMappings, orderBy);
  }

  protected List<U> retrieveMultiple(
      String fieldsSql, Filtering filtering, String sqlAdditionalJoins) throws RepositoryException {
    Map<String, Object> argumentMappings = new HashMap<>(0);
    return retrieveMultiple(fieldsSql, filtering, sqlAdditionalJoins, argumentMappings);
  }

  protected List<U> retrieveMultiple(
      String fieldsSql,
      Filtering filtering,
      String sqlAdditionalJoins,
      Map<String, Object> argumentMappings)
      throws RepositoryException {
    return retrieveMultiple(fieldsSql, filtering, sqlAdditionalJoins, argumentMappings, null);
  }

  protected List<U> retrieveMultiple(
      String fieldsSql,
      Filtering filtering,
      String sqlAdditionalJoins,
      Map<String, Object> argumentMappings,
      String innerSelect)
      throws RepositoryException {
    StringBuilder sql =
        new StringBuilder(
            "SELECT"
                + fieldsSql
                + " FROM "
                + (StringUtils.hasText(innerSelect) ? "(%s)".formatted(innerSelect) : tableName)
                + " AS "
                + tableAlias
                + (StringUtils.hasText(sqlAdditionalJoins)
                    ? " %s".formatted(sqlAdditionalJoins)
                    : "")
                + (StringUtils.hasText(getSqlSelectAllFieldsJoins())
                    ? " %s".formatted(getSqlSelectAllFieldsJoins())
                    : "")
                + (StringUtils.hasText(getSqlSelectReducedFieldsJoins())
                    ? " %s".formatted(getSqlSelectReducedFieldsJoins())
                    : ""));

    if (argumentMappings == null) {
      argumentMappings = new HashMap<>(0);
    }
    addFiltering(filtering, sql, argumentMappings);

    Map<String, Object> bindMap = Map.copyOf(argumentMappings);
    try {
      List<U> result =
          dbi.withHandle(
                  h ->
                      h.createQuery(sql.toString())
                          .bindMap(bindMap)
                          .reduceRows(
                              (Map<UUID, U> map, RowView rowView) -> {
                                fullReduceRowsBiConsumer(map, rowView);
                                additionalReduceRowsBiConsumer(map, rowView);
                              }))
              .collect(Collectors.toList());
      return result;
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  protected U retrieveOne(String fieldsSql, Filtering filtering, String sqlAdditionalJoins)
      throws RepositoryException {
    Map<String, Object> argumentMappings = new HashMap<>(0);
    return retrieveOne(fieldsSql, filtering, sqlAdditionalJoins, argumentMappings, null);
  }

  protected U retrieveOne(
      String fieldsSql,
      Filtering filtering,
      String sqlAdditionalJoins,
      Map<String, Object> argumentMappings,
      String innerSelect)
      throws RepositoryException {
    List<U> resultElements =
        retrieveMultiple(fieldsSql, filtering, sqlAdditionalJoins, argumentMappings, innerSelect);
    return resultElements.stream().findFirst().orElse(null);
  }

  @Override
  public void save(U uniqueObject, Map<String, Object> bindings)
      throws RepositoryException, ValidationException {
    save(uniqueObject, bindings, null);
  }

  public void save(
      U uniqueObject,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier)
      throws RepositoryException, ValidationException {
    if (uniqueObject == null) {
      throw new IllegalArgumentException("Given object must not be null");
    }
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }

    if (uniqueObject.getUuid() == null) {
      // in case of fileresource the uuid is created on binary upload (before metadata
      // save) to make saving on storage using uuid is possible
      uniqueObject.setUuid(UUID.randomUUID());
    }
    LocalDateTime now = LocalDateTime.now();
    if (uniqueObject.getCreated() == null) {
      uniqueObject.setCreated(now);
    }
    if (uniqueObject.getLastModified() == null) {
      uniqueObject.setLastModified(now);
    }
    boolean hasReturningStmt = !getReturnedFieldsOnInsertUpdate().isEmpty();
    String sql =
        "INSERT INTO "
            + tableName
            + "("
            + getSqlInsertFields()
            + ") VALUES ("
            + getSqlInsertValues()
            + ")"
            + (hasReturningStmt
                ? " RETURNING " + String.join(", ", getReturnedFieldsOnInsertUpdate())
                : "");
    if (sqlModifier != null) {
      sql = sqlModifier.apply(sql, bindings);
    }
    execInsertUpdate(sql, uniqueObject, bindings, hasReturningStmt);
  }

  @Override
  public void update(U uniqueObject, Map<String, Object> bindings)
      throws RepositoryException, ValidationException {
    update(uniqueObject, bindings, null);
  }

  public void update(
      U uniqueObject,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier)
      throws RepositoryException, ValidationException {
    if (uniqueObject == null) {
      throw new IllegalArgumentException("Given object must not be null");
    }
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }

    uniqueObject.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created

    boolean hasReturningStmt = !getReturnedFieldsOnInsertUpdate().isEmpty();
    String sql =
        "UPDATE "
            + tableName
            + " SET"
            + getSqlUpdateFieldValues()
            + " WHERE uuid=:uuid"
            + (hasReturningStmt
                ? " RETURNING " + String.join(", ", getReturnedFieldsOnInsertUpdate())
                : "");

    if (sqlModifier != null) {
      sql = sqlModifier.apply(sql, bindings);
    }
    execInsertUpdate(sql, uniqueObject, bindings, hasReturningStmt);
  }
}
