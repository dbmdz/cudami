package de.digitalcollections.cudami.server.backend.impl.jdbi.relation;

import de.digitalcollections.cudami.server.backend.api.repository.relation.PredicateRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.relation.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PredicateRepositoryImpl extends JdbiRepositoryImpl implements PredicateRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(PredicateRepositoryImpl.class);
  public static final String MAPPING_PREFIX = "pred";

  public static final String SQL_REDUCED_FIELDS_PRED =
      " p.value pred_value, p.label pred_label,"
          + " p.created pred_created, p.last_modified pred_lastModified";

  public static final String SQL_FULL_FIELDS_PRED =
      SQL_REDUCED_FIELDS_PRED + " , p.description pred_description";

  public static final String TABLE_ALIAS = "p";
  public static final String TABLE_NAME = "predicates";

  @Autowired
  public PredicateRepositoryImpl(Jdbi dbi) {
    super(dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX);
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
    final String sql = "SELECT * FROM " + tableName + " AS " + tableAlias;

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
    String query = "SELECT * FROM " + tableName + " WHERE value = :value";
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
    return new ArrayList<>(Arrays.asList("created", "label", "lastModified", "value"));
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
      predicate.setCreated(predicate.getLastModified());

      String createQuery =
          "INSERT INTO "
              + tableName
              + "("
              + "value, label, description,"
              + " created, last_modified"
              + ") VALUES ("
              + ":value, :label::JSONB, :description::JSONB,"
              + " :created, :lastModified"
              + ")";

      dbi.withHandle(h -> h.createUpdate(createQuery).bindBean(predicate).execute());
    }

    return findOneByValue(predicate.getValue());
  }
}
