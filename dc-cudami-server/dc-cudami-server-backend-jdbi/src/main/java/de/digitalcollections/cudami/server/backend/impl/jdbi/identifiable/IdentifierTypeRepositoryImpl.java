package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierTypeImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.List;
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

  @Autowired
  public IdentifierTypeRepositoryImpl(Jdbi dbi) {
    super(dbi, "identifiertypes", "idt", "idt");
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
    StringBuilder innerQuery = new StringBuilder("SELECT * FROM " + tableName);
    addFiltering(pageRequest, innerQuery);
    addPageRequestParams(pageRequest, innerQuery);

    final String sql = innerQuery.toString();

    List<IdentifierType> result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .mapToBean(IdentifierTypeImpl.class)
                    .map(IdentifierType.class::cast)
                    .list());

    StringBuilder sqlCount = new StringBuilder("SELECT count(*) FROM " + tableName);
    addFiltering(pageRequest, sqlCount);
    long total =
        dbi.withHandle(h -> h.createQuery(sqlCount.toString()).mapTo(Long.class).findOne().get());

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public IdentifierType findOne(UUID uuid) {
    final String sql = "SELECT * FROM " + tableName + " WHERE uuid = :uuid";

    IdentifierType identifierType =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bind("uuid", uuid)
                    .mapToBean(IdentifierTypeImpl.class)
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
                    .mapToBean(IdentifierTypeImpl.class)
                    .findOne()
                    .orElse(null));

    return identifierType;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"label", "namespace", "pattern"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "label":
        return "label";
      case "namespace":
        return "namespace";
      case "pattern":
        return "pattern";
      default:
        return null;
    }
  }

  @Override
  public IdentifierType save(IdentifierType identifierType) {
    identifierType.setUuid(UUID.randomUUID());

    final String sql =
        "INSERT INTO "
            + tableName
            + "(uuid, label, namespace, pattern)"
            + " VALUES (:uuid, :label, :namespace, :pattern)"
            + " RETURNING *";

    IdentifierType result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindBean(identifierType)
                    .mapToBean(IdentifierTypeImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public IdentifierType update(IdentifierType identifierType) {
    // do not update/left out from statement (not changed since insert): uuid
    final String sql =
        "UPDATE "
            + tableName
            + " SET label=:label, namespace=:namespace, pattern=:pattern WHERE uuid=:uuid RETURNING *";

    IdentifierType result =
        dbi.withHandle(
            h ->
                h.createQuery(sql)
                    .bindBean(identifierType)
                    .mapToBean(IdentifierTypeImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
