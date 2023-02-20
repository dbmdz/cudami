package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.semantic.TagRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.UniqueObjectRepositoryImpl;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Tag;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class TagRepositoryImpl extends UniqueObjectRepositoryImpl<Tag> implements TagRepository {

  public static final String TABLE_NAME = "tags";
  public static final String TABLE_ALIAS = "tags";
  public static final String MAPPING_PREFIX = "tags";

  public static final String SQL_INSERT_FIELDS = " uuid, value, created, last_modified";
  public static final String SQL_INSERT_VALUES = " :uuid, :value, :created, :lastModified";
  public static final String SQL_REDUCED_FIELDS_TAGS =
      String.format(
          " %1$s.uuid as %2$s_uuid, %1$s.value as %2$s_value, %1$s.created as %2$s_created, %1$s.last_modified as %2$s_last_modified",
          TABLE_ALIAS, MAPPING_PREFIX);
  public static final String SQL_FULL_FIELDS_TAGS = SQL_REDUCED_FIELDS_TAGS;

  public TagRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.dbi.registerRowMapper(BeanMapper.factory(Tag.class, MAPPING_PREFIX));
  }

  @Override
  public Tag getByUuid(UUID uuid) {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_TAGS
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE uuid = :uuid";

    Tag tag =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapTo(Tag.class).findOne().orElse(null));

    return tag;
  }

  @Override
  public Tag save(Tag tag) {
    tag.setUuid(UUID.randomUUID());
    tag.setCreated(LocalDateTime.now());
    tag.setLastModified(LocalDateTime.now());

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + SQL_INSERT_FIELDS
            + ")"
            + " VALUES ("
            + SQL_INSERT_VALUES
            + ")"
            + " RETURNING *";

    Tag result =
        dbi.withHandle(
            h -> h.createQuery(sql).bindBean(tag).mapToBean(Tag.class).findOne().orElse(null));
    return result;
  }

  @Override
  public Tag update(Tag tag) {
    tag.setLastModified(LocalDateTime.now());

    final String sql =
        "UPDATE "
            + tableName
            + " SET value=:value, last_modified=:lastModified WHERE uuid=:uuid RETURNING *";

    Tag result =
        dbi.withHandle(
            h -> h.createQuery(sql).bindBean(tag).mapToBean(Tag.class).findOne().orElse(null));
    return result;
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
    return true;
  }

  @Override
  public PageResponse<Tag> find(PageRequest pageRequest) {
    Map argumentMappings = new HashMap<>(0);
    String commonSql = " FROM " + tableName + " AS " + tableAlias;
    StringBuilder commonSqlBuilder = new StringBuilder(commonSql);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSqlBuilder, argumentMappings);
    addFiltering(pageRequest, commonSqlBuilder, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".* " + commonSqlBuilder);
    addPageRequestParams(pageRequest, innerQuery);
    List<Tag> result =
        retrieveList(
            SQL_REDUCED_FIELDS_TAGS,
            innerQuery,
            argumentMappings,
            getOrderBy(pageRequest.getSorting()));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSqlBuilder);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
  }

  @Override
  public Tag getByValue(String value) {
    final String sql =
        "SELECT " + SQL_FULL_FIELDS_TAGS + " FROM " + tableName + " WHERE value = :value";

    Tag tag =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("value", value).mapTo(Tag.class).findOne().orElse(null));

    return tag;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("created", "lastModified", "value"));
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "value":
        return tableAlias + ".value";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "uuid":
        return tableAlias + ".uuid";
      default:
        return null;
    }
  }

  @Override
  protected String getUniqueField() {
    return "uuid";
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "value":
        return true;
      default:
        return false;
    }
  }

  private List<Tag> retrieveList(
      String fieldsSql,
      StringBuilder innerQuery,
      final Map<String, Object> argumentMappings,
      String orderBy) {
    final String sql =
        "SELECT "
            + fieldsSql
            + " FROM "
            + (innerQuery != null ? "(" + innerQuery + ")" : tableName)
            + " AS "
            + tableAlias
            + (orderBy != null && orderBy.matches("(?iu)^\\s*order by.+")
                ? " " + orderBy
                : (StringUtils.hasText(orderBy) ? " ORDER BY " + orderBy : ""));

    List<Tag> result =
        dbi.withHandle(
            (Handle handle) ->
                handle
                    .createQuery(sql)
                    .bindMap(argumentMappings)
                    .mapTo(Tag.class)
                    .collect(Collectors.toList()));
    return result;
  }
}
