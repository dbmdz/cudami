package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.StatementException;
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
  public void delete(List<UUID> uuids) throws RepositoryException {
    if (uuids == null || uuids.isEmpty()) {
      return;
    }
    try {
      dbi.withHandle(
          h ->
              h.createUpdate("DELETE FROM " + tableName + " WHERE uuid in (<uuids>)")
                  .bindList("uuids", uuids)
                  .execute());
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public int deleteByIdentifiable(UUID identifiableUuid) throws RepositoryException {
    try {
      return dbi.withHandle(
          h ->
              h.createUpdate("DELETE FROM " + tableName + " WHERE identifiable = :uuid")
                  .bind("uuid", identifiableUuid)
                  .execute());
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public List<Identifier> findByIdentifiable(UUID uuidIdentifiable) throws RepositoryException {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_ID
            + " FROM "
            + tableName
            + " "
            + tableAlias
            + " WHERE identifiable = :uuid";

    try {
      return dbi.withHandle(
          h ->
              h.createQuery(sql)
                  .bind("uuid", uuidIdentifiable)
                  .mapTo(Identifier.class)
                  .collect(Collectors.toList()));
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
    return new ArrayList<>(Arrays.asList("id", "identifiable", "namespace"));
  }

  @Override
  public Identifier getByUuid(UUID identifierUuid) throws RepositoryException {
    final String sql =
        "SELECT "
            + SQL_FULL_FIELDS_ID
            + " FROM "
            + tableName
            + " "
            + tableAlias
            + " WHERE uuid = :uuid";

    try {
      return dbi.withHandle(
          h ->
              h.createQuery(sql)
                  .bind("uuid", identifierUuid)
                  .mapTo(Identifier.class)
                  .findOne()
                  .orElse(null));
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  public String getColumnName(String modelProperty) {
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
  public Identifier save(Identifier identifier) throws RepositoryException {
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

    try {
      return dbi.withHandle(
          h ->
              h.createQuery(sql)
                  .bindBean(identifier)
                  .mapToBean(Identifier.class)
                  .findOne()
                  .orElse(null));
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new RepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new RepositoryException(e);
    }
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    return false;
  }
}
