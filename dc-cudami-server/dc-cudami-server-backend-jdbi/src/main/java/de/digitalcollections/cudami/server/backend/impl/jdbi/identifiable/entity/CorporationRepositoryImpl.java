package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

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
  public CorporationRepositoryImpl(Jdbi dbi) {
    super(dbi);
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
        new StringBuilder("SELECT " + "uuid, created, description, label, last_modified" + ", text")
            .append(" FROM corporations");

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
        "SELECT "
            + "uuid, created, description, label, last_modified"
            + ", text"
            + " FROM corporations"
            + " WHERE uuid = :uuid";

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

    Corporation result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "INSERT INTO corporations(uuid, created, description, identifiable_type, label, last_modified, entity_type, text) VALUES (:uuid, :created, :description::JSONB, :type, :label::JSONB, :lastModified, :entityType, :text::JSONB) RETURNING *")
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
    Corporation result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "UPDATE corporations SET description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified, text=:text::JSONB WHERE uuid=:uuid RETURNING *")
                    .bindBean(corporation)
                    .mapToBean(CorporationImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
