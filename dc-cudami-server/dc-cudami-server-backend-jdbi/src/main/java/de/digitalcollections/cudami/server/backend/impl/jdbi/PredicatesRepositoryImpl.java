package de.digitalcollections.cudami.server.backend.impl.jdbi;

import de.digitalcollections.cudami.server.backend.api.repository.PredicatesRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Version;
import de.digitalcollections.model.api.identifiable.entity.agent.Agent;
import de.digitalcollections.model.api.identifiable.entity.work.Work;
import de.digitalcollections.model.api.relations.Predicate;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.VersionImpl;
import de.digitalcollections.model.impl.relations.PredicateImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class PredicatesRepositoryImpl implements PredicatesRepository {

  private final Jdbi dbi;

  @Autowired
  public PredicatesRepositoryImpl(Jdbi dbi) {
    this.dbi = dbi;
  }

  @Override
  public Predicate findOne(String value) {
    String query = "SELECT * FROM predicates WHERE value = :value";
    Optional<Predicate> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .bind("value", value)
                    .mapToBean(PredicateImpl.class)
                    .stream()
                    .map(Predicate.class::cast)
                    .findFirst());
    return result.orElse(null);
  }

  @Override
  public List<Predicate> findAll() {
    String query = "SELECT * FROM predicates";
    List<Predicate> result =
        dbi.withHandle(
            h ->
                h
                    .createQuery(query)
                    .mapToBean(PredicateImpl.class)
                    .stream()
                    .map(Predicate.class::cast)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public Predicate save(Predicate predicate) {
    Predicate existingPredicate = findOne(predicate.getValue());
    if (existingPredicate != null) {
      predicate.setCreated(existingPredicate.getCreated());
    }

    predicate.setLastModified(LocalDateTime.now());

    if (existingPredicate != null) {
      // Update
      String updateQuery = "UPDATE predicates SET"
          + " label=:label::JSONB, description=:description::JSONB,"
          + " created=:created, last_modified=:lastModified"
          + " WHERE value=:value";

      dbi.withHandle(
          h ->
              h.createUpdate(updateQuery)
                  .bindBean(predicate)
                  .execute());
    } else {
      // Creation
      predicate.setCreated(predicate.getLastModified());

      String createQuery =
          "INSERT INTO predicates("
              + "value, label, description,"
              + " created, last_modified"
              + ") VALUES ("
              + ":value, :label::JSONB, :description::JSONB,"
              + " :created, :lastModified"
              + ")";

      dbi.withHandle(
          h ->
              h.createUpdate(createQuery)
                  .bindBean(predicate)
                  .execute());
    }

    return predicate;
  }
}
