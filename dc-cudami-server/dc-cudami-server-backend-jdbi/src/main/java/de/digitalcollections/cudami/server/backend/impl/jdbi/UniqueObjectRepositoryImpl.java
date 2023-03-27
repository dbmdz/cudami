package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
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

  /*
   * BiFunction for reducing rows (related objects) of joins not already part of
   * identifiable (Identifier, preview image ImageFileResource).
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
    try {
      Integer integer =
          dbi.withHandle(
              h ->
                  h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                      .bindList("uuids", uuids)
                      .execute());
      return integer;
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

  /**
   * On insert or update these fields will be returned to be processed by {@link
   * #insertUpdateCallback(Identifiable, Map)}.
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
  protected String getSqlSelectAllFields() {
    return getSqlSelectAllFields(tableAlias, mappingPrefix);
  }

  protected String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    // reduced contains already all fields:
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
  protected String getSqlSelectReducedFields() {
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

    List<U> result =
        dbi.withHandle(
            (Handle handle) -> {
              // handle.execute("SET cust.code=:customerID", "bav");
              // multitenancy, see
              // https://varun-verma.medium.com/isolate-multi-tenant-data-in-postgresql-db-using-row-level-security-rls-bdd3089d9337
              // https://aws.amazon.com/de/blogs/database/multi-tenant-data-isolation-with-postgresql-row-level-security/
              // https://www.postgresql.org/docs/current/ddl-rowsecurity.html
              // https://www.postgresql.org/docs/current/sql-createpolicy.html

              return handle
                  .createQuery(sql)
                  .bindMap(argumentMappings)
                  .reduceRows(basicReduceRowsBiConsumer)
                  .collect(Collectors.toList());
            });
    return result;
  }

  protected List<U> retrieveList(
      String fieldsSql,
      StringBuilder innerQuery,
      final Map<String, Object> argumentMappings,
      String orderBy)
      throws RepositoryException {
    // no additional joins
    return retrieveList(fieldsSql, null, innerQuery, argumentMappings, orderBy);
  }
}
