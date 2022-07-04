package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.statement.StatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifierTypeRepositoryImpl extends JdbiRepositoryImpl
    implements IdentifierTypeRepository {

  public static final String MAPPING_PREFIX = "idt";

  public static final String SQL_INSERT_FIELDS =
      " uuid, created, label, namespace, pattern, last_modified";
  public static final String SQL_INSERT_VALUES =
      " :uuid, :created, :label, :namespace, :pattern, :lastModified";
  public static final String TABLE_ALIAS = "idt";
  public static final String SQL_REDUCED_FIELDS_IDT =
      String.format(
          " %1$s.uuid, %1$s.created, %1$s.label, %1$s.namespace, %1$s.pattern, %1$s.last_modified",
          TABLE_ALIAS);
  public static final String SQL_FULL_FIELDS_IDT = SQL_REDUCED_FIELDS_IDT;
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
    StringBuilder commonSql = new StringBuilder(" FROM " + tableName + " AS " + tableAlias);

    Map<String, Object> argumentMappings = new HashMap<>(0);
    String executedSearchTerm = addSearchTerm(pageRequest, commonSql, argumentMappings);

    // Actually "*" should be used in select, but here we don't need it as there is no outer select
    StringBuilder query = new StringBuilder("SELECT " + SQL_REDUCED_FIELDS_IDT + commonSql);
    addPageRequestParams(pageRequest, query);
    List<IdentifierType> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString())
                    .bindMap(argumentMappings)
                    .mapToBean(IdentifierType.class)
                    .list());

    long total = count(commonSql.toString(), argumentMappings);
    return new PageResponse<>(result, pageRequest, total, executedSearchTerm);
  }

  @Override
  public List<IdentifierType> findAll() throws RepositoryException {
    String query = "SELECT " + SQL_REDUCED_FIELDS_IDT + " FROM " + tableName + " AS " + tableAlias;
    try {
      return dbi.withHandle(h -> h.createQuery(query).mapToBean(IdentifierType.class).list());
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("label", "namespace", "pattern"));
  }

  @Override
  public IdentifierType getByNamespace(String namespace) {
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
  public IdentifierType getByUuid(UUID uuid) {
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
  protected List<String> getSearchTermTemplates(String tableAlias) {
    return new ArrayList<>(
        Arrays.asList(
            SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tableAlias, "label"),
            SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tableAlias, "namespace")));
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
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "label":
        return true;
      default:
        return false;
    }
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
