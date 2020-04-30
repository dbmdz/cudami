package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifierRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl
    implements IdentifierRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierRepositoryImpl.class);

  private final Jdbi dbi;

  @Autowired
  public IdentifierRepositoryImpl(Jdbi dbi) {
    this.dbi = dbi;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM identifiers";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Identifier> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT * FROM identifiers");

    addPageRequestParams(pageRequest, query);
    List<IdentifierImpl> result =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(IdentifierImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public List<Identifier> find(String searchTerm, int maxResults) {
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, identifiable, namespace, identifier FROM identifiers WHERE namespace ILIKE '%' || :searchTerm || '%'");
    query.append(" LIMIT :maxResults");

    List<Identifier> result =
        dbi.withHandle(
            h ->
                h.createQuery(query.toString()).bind("searchTerm", searchTerm)
                    .bind("maxResults", maxResults).mapToBean(IdentifierImpl.class).stream()
                    .map(Identifier.class::cast)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public List<Identifier> findByIdentifiable(UUID identifiable) {
    List<Identifier> result =
        dbi.withHandle(
            h ->
                h.createQuery("SELECT * FROM identifiers WHERE identifiable = :identifiable")
                    .bind("identifiable", identifiable).mapToBean(IdentifierImpl.class).stream()
                    .map(Identifier.class::cast)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public Identifier findOne(String namespace, String id) {
    Identifier identifier =
        (Identifier)
            dbi.withHandle(
                h ->
                    h.createQuery(
                            "SELECT * FROM identifiers WHERE namespace = :namespace, identifier = :identifier")
                        .bind("namespace", namespace)
                        .bind("identifier", id)
                        .mapToBean(IdentifierImpl.class)
                        .findOne()
                        .orElse(null));
    return identifier;
  }

  @Override
  public Identifier save(Identifier identifier) {
    identifier.setUuid(UUID.randomUUID());

    Identifier result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "INSERT INTO identifiers(uuid, identifiable, namespace, identifier)"
                            + " VALUES (:uuid, :identifiable, :namespace, :id) RETURNING *")
                    .bindBean(identifier)
                    .mapToBean(IdentifierImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public Identifier update(Identifier identifier) {
    throw new UnsupportedOperationException(
        "An update on identifiable, namespace and identifier has no use case.");
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"identifiable", "namespace", "id"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "identifiable":
        return "identifiable";
      case "namespace":
        return "namespace";
      case "id":
        return "id";
      default:
        return null;
    }
  }
}
