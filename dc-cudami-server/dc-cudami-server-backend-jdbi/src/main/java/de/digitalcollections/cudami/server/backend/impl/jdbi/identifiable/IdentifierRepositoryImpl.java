package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifierRepositoryImpl extends JdbiRepositoryImpl implements IdentifierRepository {

  public static final String MAPPING_PREFIX = "id";

  public static final String SQL_INSERT_FIELDS =
      " uuid, created, identifiable, namespace, identifier, last_modified";
  public static final String SQL_INSERT_VALUES =
      " :uuid, :created, :identifiable, :namespace, :id, :lastModified";
  public static final String TABLE_ALIAS = "id";
  public static final String SQL_REDUCED_FIELDS_ID =
      String.format(
          " %1$s.uuid %2$s_uuid, %1$s.created %2$s_created, %1$s.identifiable %2$s_identifiable, %1$s.namespace %2$s_namespace, %1$s.identifier %2$s_id, %1$s.last_modified %2$s_lastModified",
          TABLE_ALIAS, MAPPING_PREFIX);
  public static final String SQL_FULL_FIELDS_ID = SQL_REDUCED_FIELDS_ID;
  public static final String TABLE_NAME = "identifiers";

  @Autowired
  public IdentifierRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());

    // Hint: as repo is no extension of IdentifiableRepositoryImpl (registering mapper for
    // Identifiable in constructor), we have to register row mapper on ourselves
    dbi.registerRowMapper(BeanMapper.factory(Identifier.class, MAPPING_PREFIX));
  }

  @Override
  public void delete(List<UUID> uuids) {
    if (uuids == null || uuids.isEmpty()) {
      return;
    }
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
  }

  @Override
  public void deleteByIdentifiable(UUID identifiableUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE identifiable = :uuid")
                .bind("uuid", identifiableUuid)
                .execute());
  }

  @Override
  public PageResponse<Identifier> find(PageRequest pageRequest) {
    Map<String, Object> argumentMappings = new HashMap<>();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT " + SQL_REDUCED_FIELDS_ID + " FROM " + tableName + " AS " + tableAlias);
    addFiltering(pageRequest, innerQuery, argumentMappings);
    addPageRequestParams(pageRequest, innerQuery);

    final String sql = innerQuery.toString();

    List<Identifier> result =
        dbi.withHandle(
            h -> h.createQuery(sql).bindMap(argumentMappings).mapTo(Identifier.class).list());

    StringBuilder sqlCount = new StringBuilder("SELECT count(*) FROM " + tableName);
    addFiltering(pageRequest, sqlCount, argumentMappings);
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(sqlCount.toString())
                    .bindMap(argumentMappings)
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public SearchPageResponse<Identifier> find(SearchPageRequest searchPageRequest) {
    Map<String, Object> argumentMappings = new HashMap<>();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT "
                + SQL_REDUCED_FIELDS_ID
                + " FROM "
                + tableName
                + " AS "
                + tableAlias
                + " WHERE namespace ILIKE '%' || :searchTerm || '%'");
    addFiltering(searchPageRequest, innerQuery, argumentMappings);
    addPageRequestParams(searchPageRequest, innerQuery);

    final String sql = innerQuery.toString();

    List<Identifier> result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .bindMap(argumentMappings)
                    .mapTo(Identifier.class)
                    .list());

    StringBuilder countQuery =
        new StringBuilder(
            "SELECT count(*) FROM "
                + tableName
                + " WHERE namespace ILIKE '%' || :searchTerm || '%'");
    addFiltering(searchPageRequest, countQuery, argumentMappings);
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery.toString())
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .bindMap(argumentMappings)
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    return new SearchPageResponse<>(result, searchPageRequest, total);
  }

  @Override
  public List<Identifier> findByIdentifiable(UUID uuidIdentifiable) {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_ID
            + " FROM "
            + tableName
            + " "
            + tableAlias
            + " WHERE identifiable = :uuid";

    return dbi.withHandle(
        h ->
            h.createQuery(sql)
                .bind("uuid", uuidIdentifiable)
                .mapTo(Identifier.class)
                .collect(Collectors.toList()));
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("id", "identifiable", "namespace"));
  }

  @Override
  public Identifier getByNamespaceAndId(String namespace, String id) {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_ID
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE namespace = :namespace, identifier = :identifier";

    Identifier identifier =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bind("namespace", namespace)
                    .bind("identifier", id)
                    .mapTo(Identifier.class)
                    .findOne()
                    .orElse(null));
    return identifier;
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "id":
        return tableAlias + ".id";
      case "identifiable":
        return tableAlias + ".identifiable";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "namespace":
        return tableAlias + ".namespace";
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
  public Identifier save(Identifier identifier) {
    identifier.setUuid(UUID.randomUUID());
    identifier.setCreated(LocalDateTime.now());
    identifier.setLastModified(LocalDateTime.now());

    final String sql =
        "INSERT INTO "
            + tableName
            + "( "
            + SQL_INSERT_FIELDS
            + " )"
            + " VALUES ( "
            + SQL_INSERT_VALUES
            + " )"
            + " RETURNING *, identifier id";

    Identifier result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindBean(identifier)
                    .mapToBean(Identifier.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public Identifier getByUuid(UUID identifierUuid) {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_ID
            + " FROM "
            + tableName
            + " "
            + tableAlias
            + " WHERE uuid = :uuid";

    Identifier result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bind("uuid", identifierUuid)
                    .mapTo(Identifier.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public Identifier update(Identifier identifier) {
    throw new UnsupportedOperationException(
        "An update on identifiable, namespace and identifier has no use case.");
  }
}
