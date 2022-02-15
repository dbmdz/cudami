package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifierTypeRepositoryImpl extends JdbiRepositoryImpl
    implements IdentifierTypeRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "idt";
  public static final String SQL_INSERT_FIELDS =
      " uuid, created, identifiable, namespace, identifier, last_modified";
  public static final String SQL_INSERT_VALUES =
      " :uuid, :created, :identifiable, :namespace, :id, :lastModified";
  public static final String SQL_REDUCED_FIELDS_IDT =
      " id.uuid, id.created, id.identifiable, id.namespace, id.identifier, id.last_modified";
  public static final String SQL_FULL_FIELDS_IDT = SQL_REDUCED_FIELDS_IDT;
  public static final String TABLE_ALIAS = "idt";
  public static final String TABLE_NAME = "identifiertypes";

  @Autowired
  public IdentifierTypeRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public void delete(List<UUID> uuids) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                .bindList("uuids", uuids)
                .execute());
  }

  @Override
  public PageResponse<IdentifierType> find(PageRequest pageRequest) {
    Map<String, Object> argumentMappings = new HashMap<>();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT " + SQL_REDUCED_FIELDS_IDT + " FROM " + tableName + " AS " + tableAlias);
    addFiltering(pageRequest, innerQuery, argumentMappings);
    addPageRequestParams(pageRequest, innerQuery);

    final String sql = innerQuery.toString();

    List<IdentifierType> result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindMap(argumentMappings)
                    .mapToBean(IdentifierType.class)
                    .map(IdentifierType.class::cast)
                    .list());

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
  public IdentifierType findOne(UUID uuid) {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_IDT
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE uuid = :uuid";

    IdentifierType identifierType =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bind("uuid", uuid)
                    .mapToBean(IdentifierType.class)
                    .findOne()
                    .orElse(null));

    return identifierType;
  }

  @Override
  public IdentifierType findOneByNamespace(String namespace) {
    final String sql = "SELECT * FROM " + tableName + " WHERE namespace = :namespace";

    IdentifierType identifierType =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bind("namespace", namespace)
                    .mapToBean(IdentifierType.class)
                    .findOne()
                    .orElse(null));

    return identifierType;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("label", "namespace", "pattern"));
  }

  @Override
  protected String getColumnName(String modelProperty) {
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
      case "namespace":
        return tableAlias + ".namespace";
      case "pattern":
        return tableAlias + ".pattern";
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
  public IdentifierType save(IdentifierType identifierType) {
    identifierType.setUuid(UUID.randomUUID());
    identifierType.setCreated(LocalDateTime.now());
    identifierType.setLastModified(LocalDateTime.now());

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

    IdentifierType result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindBean(identifierType)
                    .mapToBean(IdentifierType.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public IdentifierType update(IdentifierType identifierType) {
    identifierType.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert): uuid
    final String sql =
        "UPDATE "
            + tableName
            + " SET label=:label, last_modified=:lastModified, namespace=:namespace, pattern=:pattern WHERE uuid=:uuid RETURNING *";

    IdentifierType result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindBean(identifierType)
                    .mapToBean(IdentifierType.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
