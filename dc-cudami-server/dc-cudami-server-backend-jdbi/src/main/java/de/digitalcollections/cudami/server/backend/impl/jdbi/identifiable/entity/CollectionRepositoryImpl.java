package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CollectionRepository;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Collection;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.CollectionImpl;
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
public class CollectionRepositoryImpl extends EntityRepositoryImpl<Collection>
    implements CollectionRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectionRepositoryImpl.class);

  @Autowired
  public CollectionRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM collections";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Collection> find(PageRequest pageRequest) {
    StringBuilder query =
        new StringBuilder("SELECT " + "uuid, created, description, label, last_modified" + ", text")
            .append(" FROM collections");

    addPageRequestParams(pageRequest, query);

    List<CollectionImpl> result =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(CollectionImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Collection findOne(UUID uuid) {
    String query =
        "SELECT "
            + "uuid, created, description, label, last_modified"
            + ", text"
            + " FROM collections"
            + " WHERE uuid = :uuid";

    Collection collection =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(CollectionImpl.class)
                    .findOne()
                    .orElse(null));
    return collection;
  }

  @Override
  public Collection findOne(Identifier identifier) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"uuid"};
  }

  @Override
  public Collection save(Collection collection) {
    collection.setUuid(UUID.randomUUID());
    collection.setCreated(LocalDateTime.now());
    collection.setLastModified(LocalDateTime.now());

    Collection result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "INSERT INTO collections(uuid, created, description, identifiable_type, label, last_modified, entity_type, text) VALUES (:uuid, :created, :description::JSONB, :type, :label::JSONB, :lastModified, :entityType, :text::JSONB) RETURNING *")
                    .bindBean(collection)
                    .mapToBean(CollectionImpl.class)
                    .findOne()
                    .orElse(null));

    return result;
  }

  @Override
  public Collection update(Collection collection) {
    collection.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert): uuid, created,
    // identifiable_type, entity_type
    Collection result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "UPDATE collections SET description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified, text=:text::JSONB WHERE uuid=:uuid RETURNING *")
                    .bindBean(collection)
                    .mapToBean(CollectionImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
