package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.semantic;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.DbIdentifierMapper;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Subject;
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
public class SubjectRepositoryImpl extends JdbiRepositoryImpl implements SubjectRepository {

  public static final String TABLE_NAME = "subjects";
  public static final String TABLE_ALIAS = "subj";
  public static final String MAPPING_PREFIX = "subj";

  public static final String SQL_INSERT_FIELDS =
      " uuid, label, type, identifiers, created, last_modified";
  public static final String SQL_INSERT_VALUES =
      " :uuid, :label::JSONB, :type, :identifiers, :created, :lastModified";
  public static final String SQL_REDUCED_FIELDS_SUBJECTS =
      String.format(
          " %1$s.uuid as %2$s_uuid, %1$s.label as %2$s_label, %1$s.identifiers as %2$s_identifiers, %1$s.type as %2$s_type, %1$s.created as %2$s_created, %1$s.last_modified as %2$s_last_modified",
          TABLE_ALIAS, MAPPING_PREFIX);
  public static final String SQL_FULL_FIELDS_SUBJECTS = SQL_REDUCED_FIELDS_SUBJECTS;

  public SubjectRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    this.dbi.registerRowMapper(BeanMapper.factory(Subject.class, MAPPING_PREFIX));
    this.dbi.registerArrayType(new DbIdentifierMapper());
    this.dbi.registerColumnMapper(Identifier.class, new DbIdentifierMapper());
  }

  @Override
  public Subject getByUuid(UUID uuid) {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_SUBJECTS
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE uuid = :uuid";

    Subject subject =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapTo(Subject.class).findOne().orElse(null));

    return subject;
  }

  @Override
  public Subject getByTypeAndIdentifier(String type, String namespace, String id) {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_SUBJECTS
            + " FROM "
            + tableName
            + " "
            + tableAlias
            + ", UNNEST("
            + tableAlias
            + ".identifiers) subjids"
            + String.format(" WHERE %s.type = :type", tableAlias)
            + " AND subjids.namespace = :namespace"
            + " AND subjids.id = :id";


    Subject subject =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bind("type", type)
                    .bind("namespace", namespace)
                    .bind("id", id)
                    .mapTo(Subject.class)
                    .findOne()
                    .orElse(null));

    return subject;
  }

  @Override
  public Subject save(Subject subject) {
    subject.setUuid(UUID.randomUUID());
    subject.setCreated(LocalDateTime.now());
    subject.setLastModified(LocalDateTime.now());

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

    return dbi.withHandle(
        h -> h.createQuery(sql).bindBean(subject).mapToBean(Subject.class).findOne().orElse(null));
  }


  @Override
  public Subject update(Subject subject) {
    subject.setLastModified(LocalDateTime.now());

    final String sql =
        "UPDATE "
            + tableName
            + " SET label=:label::JSONB, last_modified=:lastModified, identifiers=:identifiers, type=:type WHERE uuid=:uuid RETURNING *";

    Subject result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindBean(subject)
                    .mapToBean(Subject.class)
                    .findOne()
                    .orElse(null));
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
  public PageResponse<Subject> find(PageRequest pageRequest) {
    Map argumentMappings = new HashMap<>(0);
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + ", unnest(identifiers) as "
            + tableAlias
            + "_identifier";
    StringBuilder commonSqlBuilder = new StringBuilder(commonSql);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSqlBuilder, argumentMappings);
    addFiltering(pageRequest, commonSqlBuilder, argumentMappings);

    StringBuilder innerQuery = new StringBuilder("SELECT " + tableAlias + ".* " + commonSqlBuilder);
    addPageRequestParams(pageRequest, innerQuery);
    List<Subject> result =
        retrieveList(
            SQL_REDUCED_FIELDS_SUBJECTS,
            innerQuery,
            argumentMappings,
            getOrderBy(pageRequest.getSorting()));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSqlBuilder);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(
        Arrays.asList(
            "created",
            "label",
            tableAlias + "_identifier.namespace",
            tableAlias + "_identifier.id",
            "type",
            "lastModified"));
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
      case "identifiers_namespace":
        return tableAlias + "_identifier.namespace";
      case "identifiers_id":
        return tableAlias + "_identifier.id";
      case "type":
        return tableAlias + ".type";
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
    // TODO Auto-generated method stub
    return false;
  }

  private long retrieveCount(StringBuilder sqlCount, final Map<String, Object> argumentMappings) {
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

  private List<Subject> retrieveList(
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

    List<Subject> result =
        dbi.withHandle(
            (Handle handle) ->
                handle
                    .createQuery(sql)
                    .bindMap(argumentMappings)
                    .mapTo(Subject.class)
                    .collect(Collectors.toList()));
    return result;
  }
}
