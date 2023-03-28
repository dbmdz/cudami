package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.StatementException;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public abstract class UniqueObjectRepositoryImpl<U extends UniqueObject>
    extends JdbiRepositoryImpl<U> implements UniqueObjectRepository<U> {

  private static final Logger LOGGER = LoggerFactory.getLogger(UniqueObjectRepositoryImpl.class);

  /*
   * BiFunction for reducing rows (related objects) of joins not already part of
   * uniqueobject.
   */
  protected final BiConsumer<Map<UUID, U>, RowView> additionalReduceRowsBiConsumer;
  protected final BiConsumer<Map<UUID, U>, RowView> basicReduceRowsBiConsumer;
  protected final BiConsumer<Map<UUID, U>, RowView> fullReduceRowsBiConsumer;
  protected final Class<? extends UniqueObject> uniqueObjectImplClass;

  protected UniqueObjectRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends UniqueObject> uniqueObjectImplClass,
      int offsetForAlternativePaging) {
    super(dbi, tableName, tableAlias, mappingPrefix, offsetForAlternativePaging);

    dbi.registerRowMapper(BeanMapper.factory(uniqueObjectImplClass, mappingPrefix));
    this.uniqueObjectImplClass = uniqueObjectImplClass;

    this.additionalReduceRowsBiConsumer = createAdditionalReduceRowsBiConsumer();
    this.basicReduceRowsBiConsumer = createBasicReduceRowsBiConsumer();
    this.fullReduceRowsBiConsumer = createFullReduceRowsBiConcumer();
  }

  /**
   * For details select (only used in find one, not lists): if additional objects should be "joined"
   * into instance, set bi function for doing this.
   *
   * @return BiConsumer function
   */
  protected BiConsumer<Map<UUID, U>, RowView> createAdditionalReduceRowsBiConsumer() {
    return (map, rowView) -> {};
  }

  /**
   * Create basic reduce rows biconsumer for reduced selects (lists, paging)
   *
   * @return BiConsumer function
   */
  protected BiConsumer<Map<UUID, U>, RowView> createBasicReduceRowsBiConsumer() {
    return (map, rowView) -> {
      U uniqueObject =
          map.computeIfAbsent(
              rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
              fn -> {
                return (U) rowView.getRow(uniqueObjectImplClass);
              });
    };
  }

  /**
   * Create full reduce rows biconsumer for full selects (find one).<br>
   * For a plain UniqueObject full ad basic are the same (minimal dataset of uuid, lastModified,
   * created).
   *
   * @return BiConsumer function
   */
  protected BiConsumer<Map<UUID, U>, RowView> createFullReduceRowsBiConcumer() {
    return (map, rowView) -> {
      U uniqueObject =
          map.computeIfAbsent(
              rowView.getColumn(mappingPrefix + "_uuid", UUID.class),
              fn -> {
                return (U) rowView.getRow(uniqueObjectImplClass);
              });
    };
  }

  @Override
  public boolean deleteByUuid(UUID uuid) throws RepositoryException {
    // same performance as delete by where uuid = :uuid
    return deleteByUuids(List.of(uuid)) > 0 ? true : false;
  }

  @Override
  public int deleteByUuids(List<UUID> uuids) throws RepositoryException {
    Update update =
        dbi.withHandle(
            h ->
                h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                    .bindList("uuids", uuids));
    return execDelete(update);
  }

  private void execInsertUpdate(
      final String sql, U uniqueObject, final Map<String, Object> bindings, boolean withCallback)
      throws RepositoryException {
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
      if (bindings == null && basicReduceRowsBiConsumer == null) {
        return (List<U>)
            dbi.withHandle(
                (Handle handle) -> handle.createQuery(sql).mapToBean(uniqueObjectImplClass).list());
      } else if (bindings == null && basicReduceRowsBiConsumer != null) {
        return dbi.withHandle(
            (Handle handle) ->
                handle
                    .createQuery(sql)
                    .reduceRows(basicReduceRowsBiConsumer)
                    .collect(Collectors.toList()));
      } else if (bindings != null && basicReduceRowsBiConsumer == null) {
        return (List<U>)
            dbi.withHandle(
                (Handle handle) -> handle.createQuery(sql).mapToBean(uniqueObjectImplClass).list());
      }
      // bindings != null && basicReduceRowsBiConsumer != null
      return (List<U>)
          dbi.withHandle(
              (Handle handle) ->
                  handle
                      .createQuery(sql)
                      .bindMap(bindings)
                      .reduceRows(basicReduceRowsBiConsumer)
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
  public U getByUuidAndFiltering(UUID uuid, Filtering filtering) throws RepositoryException {
    if (filtering == null) {
      filtering = Filtering.builder().build();
    }
    filtering.add(FilterCriterion.builder().withExpression("uuid").isEquals(uuid).build());

    U result = retrieveOne(getSqlSelectAllFields(), filtering, null);
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

  public int getIndex(List<UUID> list, UUID uuid) {
    int pos = -1;
    for (UUID u : list) {
      pos += 1;
      if (u.equals(uuid)) {
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
   * @return SQL for fields of full field set of {@code UniqueObject}
   */
  public String getSqlSelectAllFields() {
    return getSqlSelectAllFields(tableAlias, mappingPrefix);
  }

  /**
   * SQL-snippet for fields to be returned for complete field request.<br>
   * If already all fields are returned with reduced fields request: just return reduced field set
   * here, otherwise add additional fields to reduced set to get all fields.
   *
   * @param tableAlias alias for database table
   * @param mappingPrefix jdbi mapping prefix for fields
   * @return SQL snippet
   */
  protected String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    // reduced contains already all fields (otherwise override this method):
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  /**
   * @return SQL for joined fields for complete field set from other table(s) (optional). Default:
   *     ""
   */
  protected String getSqlSelectAllFieldsJoins() {
    return "";
  }

  /**
   * @return SQL for fields of reduced field set of {@code UniqueObject}
   */
  public String getSqlSelectReducedFields() {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  protected String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return " "
        + tableAlias
        + ".uuid "
        + mappingPrefix
        + "_uuid, "
        + tableAlias
        + ".created "
        + mappingPrefix
        + "_created, "
        + tableAlias
        + ".last_modified "
        + mappingPrefix
        + "_lastModified";
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

  protected U retrieveOne(String fieldsSql, Filtering filtering, String sqlAdditionalJoins)
      throws RepositoryException {
    Map<String, Object> argumentMappings = new HashMap<>(0);
    return retrieveOne(fieldsSql, filtering, sqlAdditionalJoins, argumentMappings);
  }

  protected U retrieveOne(
      String fieldsSql,
      Filtering filtering,
      String sqlAdditionalJoins,
      Map<String, Object> argumentMappings)
      throws RepositoryException {
    return retrieveOne(fieldsSql, filtering, sqlAdditionalJoins, argumentMappings, null);
  }

  protected U retrieveOne(
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
                + (StringUtils.hasText(innerSelect) ? innerSelect : tableName)
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
    // TODO: getSqlSelectReducedFieldsJoins necessary if already
    // getSqlSelectAllFieldsJoins is inserted? UseCase?

    if (argumentMappings == null) {
      argumentMappings = new HashMap<>(0);
    }
    addFiltering(filtering, sql, argumentMappings);

    Map<String, Object> bindMap = Map.copyOf(argumentMappings);
    try {
      U result =
          dbi.withHandle(
                  h ->
                      h.createQuery(sql.toString())
                          .bindMap(bindMap)
                          .reduceRows(
                              (Map<UUID, U> map, RowView rowView) -> {
                                fullReduceRowsBiConsumer.accept(map, rowView);
                                additionalReduceRowsBiConsumer.accept(map, rowView);
                              }))
              .findFirst()
              .orElse(null);
      return result;
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public void save(U uniqueObject, Map<String, Object> bindings) throws RepositoryException {
    save(uniqueObject, bindings, null);
  }

  public void save(
      U uniqueObject,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier)
      throws RepositoryException {
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }

    if (uniqueObject.getUuid() == null) {
      // in case of fileresource the uuid is created on binary upload (before metadata
      // save) to make saving on storage using uuid is possible
      uniqueObject.setUuid(UUID.randomUUID());
    }
    if (uniqueObject.getCreated() == null) {
      uniqueObject.setCreated(LocalDateTime.now());
    }
    if (uniqueObject.getLastModified() == null) {
      uniqueObject.setLastModified(LocalDateTime.now());
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
  public void update(U uniqueObject, Map<String, Object> bindings) throws RepositoryException {
    update(uniqueObject, bindings, null);
  }

  public void update(
      U uniqueObject,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier)
      throws RepositoryException {
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }

    uniqueObject.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created

    boolean hasReturningStmt = !getReturnedFieldsOnInsertUpdate().isEmpty();
    // TODO: test. shouldn't it be RETURNING *  by default?
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
