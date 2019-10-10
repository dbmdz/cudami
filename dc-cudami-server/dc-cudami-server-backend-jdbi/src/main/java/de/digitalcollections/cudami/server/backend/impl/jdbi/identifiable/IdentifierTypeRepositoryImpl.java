package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifierTypeImpl;
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
public class IdentifierTypeRepositoryImpl extends AbstractPagingAndSortingRepositoryImpl
    implements IdentifierTypeRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeRepositoryImpl.class);

  private final Jdbi dbi;

  @Autowired
  public IdentifierTypeRepositoryImpl(Jdbi dbi) {
    this.dbi = dbi;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM identifiertypes";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<IdentifierType> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT * FROM identifiertypes");

    addPageRequestParams(pageRequest, query);
    List<IdentifierTypeImpl> result =
        dbi.withHandle(
            h -> h.createQuery(query.toString()).mapToBean(IdentifierTypeImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public IdentifierType findByNamespace(String namespace) {
    IdentifierType identifierType =
        (IdentifierType)
            dbi.withHandle(
                h ->
                    h.createQuery("SELECT * FROM identifiertypes WHERE namespace = :namespace")
                        .bind("namespace", namespace).mapToBean(IdentifierTypeImpl.class).stream()
                        .map(IdentifierType.class::cast)
                        .collect(Collectors.toList()));
    return identifierType;
  }

  @Override
  public IdentifierType findOne(UUID uuid) {
    IdentifierType identifierType =
        (IdentifierType)
            dbi.withHandle(
                h ->
                    h.createQuery("SELECT * FROM identifiertypes WHERE uuid = :uuid")
                        .bind("uuid", uuid)
                        .mapToBean(IdentifierTypeImpl.class)
                        .findOne()
                        .orElse(null));
    return identifierType;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"namespace"};
  }

  @Override
  public IdentifierType save(IdentifierType identifierType) {
    identifierType.setUuid(UUID.randomUUID());

    IdentifierType result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "INSERT INTO identifiertypes(uuid, label, namespace, pattern)"
                            + " VALUES (:uuid, :label, :namespace, :pattern) RETURNING *")
                    .bindBean(identifierType)
                    .mapToBean(IdentifierTypeImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public IdentifierType update(IdentifierType identifierType) {
    // do not update/left out from statement (not changed since insert): uuid
    IdentifierType result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "UPDATE identifiertypes SET label=:label, namespace=:namespace, pattern=:pattern WHERE uuid=:uuid RETURNING *")
                    .bindBean(identifierType)
                    .mapToBean(IdentifierTypeImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
