package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CorporationRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.CorporationImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CorporationRepositoryImpl extends EntityRepositoryImpl<Corporation>
    implements CorporationRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CorporationRepositoryImpl.class);

  @Autowired
  public CorporationRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM corporations";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Corporation> find(PageRequest pageRequest) {
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, label, description, created, last_modified, text FROM corporations");
    addPageRequestParams(pageRequest, query);

    List<CorporationImpl> result =
        dbi.withHandle(
            h -> h.createQuery(query.toString()).mapToBean(CorporationImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Corporation findOne(UUID uuid) {
    String query =
        "SELECT uuid, label, description, created, last_modified, text FROM corporations WHERE uuid = :uuid";
    Corporation corporation =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(CorporationImpl.class)
                    .findOne()
                    .orElse(null));
    return corporation;
  }

  @Override
  public Corporation findOne(Identifier identifier) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"uuid"};
  }

  @Override
  public Corporation save(Corporation corporation) {
    corporation.setUuid(UUID.randomUUID());
    corporation.setCreated(LocalDateTime.now());
    corporation.setLastModified(LocalDateTime.now());

    String query =
        "INSERT INTO corporations("
            + "uuid, label, description, identifiable_type, entity_type, created, last_modified, text"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :type, :entityType, :created, :lastModified, :text::JSONB"
            + ") RETURNING *";
    Corporation result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(corporation)
                    .mapToBean(CorporationImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public Corporation update(Corporation corporation) {
    corporation.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert): uuid, created,
    // identifiable_type, entity_type
    String query =
        "UPDATE corporations SET"
            + " label=:label::JSONB, description=:description::JSONB, last_modified=:lastModified, text=:text::JSONB"
            + " WHERE uuid=:uuid"
            + " RETURNING *";
    Corporation result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(corporation)
                    .mapToBean(CorporationImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
