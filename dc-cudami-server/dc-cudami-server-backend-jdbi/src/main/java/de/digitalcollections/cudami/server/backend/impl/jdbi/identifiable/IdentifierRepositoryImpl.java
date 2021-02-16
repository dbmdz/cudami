package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.paging.SearchPageResponseImpl;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifierRepositoryImpl extends JdbiRepositoryImpl implements IdentifierRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "id";
  public static final String SQL_FULL_FIELDS_ID =
      " id.uuid id_uuid, id.identifiable id_identifiable, id.namespace id_namespace, id.identifier id_id";
  public static final String TABLE_ALIAS = "id";
  public static final String TABLE_NAME = "identifiers";

  @Autowired
  public IdentifierRepositoryImpl(Jdbi dbi) {
    super(dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX);
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
  public void deleteByIdentifiable(UUID identifiableUuid) {
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM " + tableName + " WHERE identifiable = :uuid")
                .bind("uuid", identifiableUuid)
                .execute());
  }

  @Override
  public PageResponse<Identifier> find(PageRequest pageRequest) {
    StringBuilder innerQuery = new StringBuilder("SELECT * FROM " + tableName);
    addFiltering(pageRequest, innerQuery);
    addPageRequestParams(pageRequest, innerQuery);

    final String sql = innerQuery.toString();

    List<Identifier> result =
        dbi.withHandle(h ->
                h.createQuery(sql)
                    .mapToBean(Identifier.class)
                    .map(Identifier.class::cast)
                    .list());

    StringBuilder sqlCount = new StringBuilder("SELECT count(*) FROM " + tableName);
    addFiltering(pageRequest, sqlCount);
    long total =
        dbi.withHandle(h -> h.createQuery(sqlCount.toString()).mapTo(Long.class).findOne().get());

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public SearchPageResponse<Identifier> find(SearchPageRequest searchPageRequest) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM " + tableName + " WHERE namespace ILIKE '%' || :searchTerm || '%'");
    addFiltering(searchPageRequest, innerQuery);
    addPageRequestParams(searchPageRequest, innerQuery);

    final String sql = innerQuery.toString();

    List<Identifier> result =
        dbi.withHandle(h ->
                h.createQuery(sql)
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .mapToBean(Identifier.class)
                    .map(Identifier.class::cast)
                    .list());

    StringBuilder countQuery =
        new StringBuilder(
            "SELECT count(*) FROM "
                + tableName
                + " WHERE namespace ILIKE '%' || :searchTerm || '%'");
    addFiltering(searchPageRequest, countQuery);
    long total =
        dbi.withHandle(
            h ->
                h.createQuery(countQuery.toString())
                    .bind("searchTerm", searchPageRequest.getQuery())
                    .mapTo(Long.class)
                    .findOne()
                    .get());

    return new SearchPageResponseImpl<>(result, searchPageRequest, total);
  }

  @Override
  public List<Identifier> findByIdentifiable(UUID uuidIdentifiable) {
    final String sql = "SELECT * FROM " + tableName + " WHERE identifiable = :uuid";

    List<Identifier> result =
        dbi.withHandle(h ->
                h.createQuery(sql)
                    .bind("uuid", uuidIdentifiable)
                    .mapToBean(Identifier.class)
                    .map(Identifier.class::cast)
                    .collect(Collectors.toList()));
    return result;
  }

  @Override
  public Identifier findOne(String namespace, String id) {
    final String sql =
        "SELECT * FROM " + tableName + " WHERE namespace = :namespace, identifier = :identifier";

    Identifier identifier =
        dbi.withHandle(h ->
                h.createQuery(sql)
                    .bind("namespace", namespace)
                    .bind("identifier", id)
                    .mapToBean(Identifier.class)
                    .findOne()
                    .orElse(null));
    return identifier;
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return Arrays.asList("id", "identifiable", "namespace");
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "id":
        return "id";
      case "identifiable":
        return "identifiable";
      case "namespace":
        return "namespace";
      default:
        return null;
    }
  }

  @Override
  public Identifier save(Identifier identifier) {
    identifier.setUuid(UUID.randomUUID());

    final String sql =
        "INSERT INTO "
            + tableName
            + "(uuid, identifiable, namespace, identifier)"
            + " VALUES (:uuid, :identifiable, :namespace, :id)"
            + " RETURNING *";

    Identifier result =
        dbi.withHandle(h ->
                h.createQuery(sql)
                    .bindBean(identifier)
                    .mapToBean(Identifier.class)
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
