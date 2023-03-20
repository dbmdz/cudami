package de.digitalcollections.cudami.server.backend.impl.jdbi.relation;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.SearchTermTemplates;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.relation.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class PredicateRepositoryImpl extends JdbiRepositoryImpl<Predicate>
    implements PredicateRepository {

  public static final String MAPPING_PREFIX = "pred";

  public static final String SQL_INSERT_FIELDS =
      " value, label, description, created, last_modified, uuid";
  public static final String SQL_INSERT_VALUES =
      " :value, :label::JSONB, :description::JSONB, :created, :lastModified, :uuid";
  public static final String TABLE_ALIAS = "pred";
  // FIXME: using the mapping prefix leads to mapping issues (the corresponding test in
  // EntityRelationRepositoryTest#L71 fails)
  public static final String SQL_REDUCED_FIELDS_PRED =
      String.format(
          " %1$s.uuid, %1$s.value, %1$s.label, %1$s.created, %1$s.last_modified", TABLE_ALIAS);
  public static final String SQL_FULL_FIELDS_PRED =
      SQL_REDUCED_FIELDS_PRED + String.format(", %s.description", TABLE_ALIAS);
  public static final String TABLE_NAME = "predicates";

  @Autowired
  public PredicateRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public boolean deleteByUuid(UUID uuid) {
    int count =
        dbi.withHandle(
            h ->
                h.createUpdate("DELETE FROM " + tableName + " WHERE uuid=:uuid")
                    .bind("uuid", uuid)
                    .execute());
    return count == 1;
  }

  @Override
  public boolean deleteByValue(String value) {
    int count =
        dbi.withHandle(
            h ->
                h.createUpdate("DELETE FROM " + tableName + " WHERE value = :value")
                    .bind("value", value)
                    .execute());
    return count == 1;
  }

  @Override
  public PageResponse<Predicate> find(PageRequest pageRequest) {
    return find(pageRequest, null, null);
  }

  protected PageResponse<Predicate> find(
      PageRequest pageRequest, String commonSql, Map<String, Object> argumentMappings) {
    if (argumentMappings == null) {
      argumentMappings = new HashMap<>(0);
    }
    if (commonSql == null) {
      commonSql = " FROM " + tableName + " AS " + tableAlias;
    }
    StringBuilder commonSqlBuilder = new StringBuilder(commonSql);
    addFiltering(pageRequest, commonSqlBuilder, argumentMappings);
    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".*" + commonSqlBuilder);
    addPagingAndSorting(pageRequest, innerQuery);

    String orderBy = getOrderBy(pageRequest.getSorting());
    if (StringUtils.hasText(orderBy)) {
      orderBy = " ORDER BY " + orderBy;
    }
    List<Predicate> result =
        retrieveList(SQL_REDUCED_FIELDS_PRED, innerQuery, argumentMappings, orderBy);

    StringBuilder sqlCount = new StringBuilder("SELECT count(*)" + commonSqlBuilder);
    long total = retrieveCount(sqlCount, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public List<Predicate> getAll() {
    final String sql =
        "SELECT " + SQL_REDUCED_FIELDS_PRED + " FROM " + tableName + " AS " + tableAlias;

    List<Predicate> result =
        dbi.withHandle(
            h -> h.createQuery(sql).mapToBean(Predicate.class).collect(Collectors.toList()));
    return result;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("created", "label", "lastModified"));
  }

  @Override
  public Predicate getByUuid(UUID uuid) {
    String query = "SELECT * FROM " + tableName + " WHERE uuid=:uuid";
    return dbi.withHandle(
        h ->
            h.createQuery(query)
                .bind("uuid", uuid)
                .mapToBean(Predicate.class)
                .findOne()
                .orElse(null));
  }

  @Override
  public Predicate getByValue(String value) {
    String query =
        "SELECT "
            + SQL_FULL_FIELDS_PRED
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE value = :value";
    Optional<Predicate> result =
        dbi.withHandle(
            h -> h.createQuery(query).bind("value", value).mapToBean(Predicate.class).findFirst());
    return result.orElse(null);
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "label":
        return tableAlias + ".label";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "uuid":
        return tableAlias + ".uuid";
      case "value":
        return tableAlias + ".value";
      default:
        return null;
    }
  }

  @Override
  public List<Locale> getLanguages() {
    String query =
        "SELECT DISTINCT jsonb_object_keys("
            + tableAlias
            + ".label) as languages FROM "
            + tableName
            + " AS "
            + tableAlias;
    return dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
  }

  @Override
  protected List<String> getSearchTermTemplates(String tblAlias, String originalSearchTerm) {
    if (originalSearchTerm == null) {
      return Collections.EMPTY_LIST;
    }
    List<String> searchTermTemplates = super.getSearchTermTemplates(tblAlias, originalSearchTerm);
    searchTermTemplates.add(SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tblAlias, "value"));
    return searchTermTemplates;
  }

  @Override
  protected String getUniqueField() {
    return "value";
  }

  @Override
  protected long retrieveCount(StringBuilder sqlCount, Map<String, Object> argumentMappings) {
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

  private List<Predicate> retrieveList(
      String fieldsSql,
      StringBuilder innerQuery,
      Map<String, Object> argumentMappings,
      String orderBy) {
    final String sql =
        "SELECT "
            + fieldsSql
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (orderBy != null ? " " + orderBy : "");

    List<Predicate> result =
        dbi.withHandle(
            (Handle handle) -> {
              return handle
                  .createQuery(sql)
                  .bindMap(argumentMappings)
                  .mapToBean(Predicate.class)
                  .collect(Collectors.toList());
            });
    return result;
  }

  @Override
  public Predicate save(Predicate predicate) {
    LocalDateTime now = LocalDateTime.now();
    predicate.setUuid(UUID.randomUUID());
    predicate.setCreated(now);
    predicate.setLastModified(now);

    String createQuery =
        "INSERT INTO "
            + tableName
            + "("
            + SQL_INSERT_FIELDS
            + ") VALUES ("
            + SQL_INSERT_VALUES
            + ")";

    dbi.withHandle(h -> h.createUpdate(createQuery).bindBean(predicate).execute());
    return getByUuid(predicate.getUuid());
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "label":
      case "description":
        return true;
      default:
        return false;
    }
  }

  @Override
  public Predicate update(Predicate predicate) {
    predicate.setLastModified(LocalDateTime.now());

    String query =
        "UPDATE "
            + tableName
            + " SET value=:value, label=:label::JSONB, description=:description::JSONB, last_modified=:lastModified"
            + " WHERE uuid=:uuid RETURNING *";
    return dbi.withHandle(
        h ->
            h.createQuery(query)
                .bindBean(predicate)
                .mapToBean(Predicate.class)
                .findOne()
                .orElse(null));
  }
}
