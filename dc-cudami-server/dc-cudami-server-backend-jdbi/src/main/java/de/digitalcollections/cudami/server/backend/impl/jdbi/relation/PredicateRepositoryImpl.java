package de.digitalcollections.cudami.server.backend.impl.jdbi.relation;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.relation.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PredicateRepositoryImpl extends JdbiRepositoryImpl implements PredicateRepository {

  public static final String MAPPING_PREFIX = "pred";
  public static final String TABLE_ALIAS = "pred";
  public static final String TABLE_NAME = "predicates";

  public static final String SQL_INSERT_FIELDS =
      " value, label, description, created, last_modified, uuid";
  public static final String SQL_INSERT_VALUES =
      " :value, :label::JSONB, :description::JSONB, :created, :lastModified, :uuid";
  public static final String SQL_REDUCED_FIELDS_PRED =
      String.format(
          " %1$s.uuid, %1$s.value, %1$s.label, %1$s.created, %1$s.last_modified", TABLE_ALIAS);
  public static final String SQL_FULL_FIELDS_PRED =
      SQL_REDUCED_FIELDS_PRED + String.format(", %s.description", TABLE_ALIAS);

  @Autowired
  public PredicateRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public void delete(String value) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE value = :value")
                .bind("value", value)
                .execute());
  }

  @Override
  public List<Predicate> findAll() {
    final String sql =
        "SELECT " + SQL_REDUCED_FIELDS_PRED + " FROM " + tableName + " AS " + tableAlias;

    List<Predicate> result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .mapToBean(Predicate.class)
                    .map(Predicate.class::cast)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public Predicate findOneByValue(String value) {
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
            h ->
                h.createQuery(query)
                    .bind("value", value)
                    .mapToBean(Predicate.class)
                    .map(Predicate.class::cast)
                    .findFirst());
    return result.orElse(null);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return new ArrayList<>(Arrays.asList("created", "label", "lastModified"));
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
      case "uuid":
        return tableAlias + ".uuid";
      case "value":
        return tableAlias + ".value";
      default:
        return null;
    }
  }

  @Override
  protected String getUniqueField() {
    return "value";
  }

  @Override
  public Predicate save(Predicate predicate) {
    Predicate existingPredicate = findOneByValue(predicate.getValue());
    if (existingPredicate != null) {
      predicate.setCreated(existingPredicate.getCreated());
    }

    predicate.setLastModified(LocalDateTime.now());

    if (existingPredicate != null) {
      // Update
      String updateQuery =
          "UPDATE "
              + tableName
              + " SET"
              + " label=:label::JSONB, description=:description::JSONB,"
              + " last_modified=:lastModified"
              + " WHERE value=:value";

      dbi.withHandle(h -> h.createUpdate(updateQuery).bindBean(predicate).execute());
    } else {
      // Creation
      predicate.setUuid(UUID.randomUUID());
      predicate.setCreated(predicate.getLastModified());

      String createQuery =
          "INSERT INTO "
              + tableName
              + "("
              + SQL_INSERT_FIELDS
              + ") VALUES ("
              + SQL_INSERT_VALUES
              + ")";

      dbi.withHandle(h -> h.createUpdate(createQuery).bindBean(predicate).execute());
    }

    return findOneByValue(predicate.getValue());
  }
}
