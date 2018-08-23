package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.paging.impl.PageResponseImpl;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifiableRepositoryImpl<I extends Identifiable> extends AbstractPagingAndSortingRepositoryImpl implements IdentifiableRepository<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableRepositoryImpl.class);

  @Autowired
  protected Jdbi dbi;

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM identifiables";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public I create() {
    return (I) new IdentifiableImpl();
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT * FROM identifiables");

    addPageRequestParams(pageRequest, query);
    List<IdentifiableImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(IdentifiableImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public I findOne(UUID uuid) {
    List<? extends Identifiable> list = dbi.withHandle(h -> h.createQuery(
            "SELECT * FROM identifiables WHERE uuid = :uuid")
            .bind("uuid", uuid)
            .mapToBean(IdentifiableImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    return (I) list.get(0);
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "type", "lastModified"};
  }

  @Override
  public I save(I identifiable) {
    identifiable.setUuid(UUID.randomUUID());
    identifiable.setCreated(LocalDateTime.now());
    identifiable.setLastModified(LocalDateTime.now());

    IdentifiableImpl result = null;

    result = dbi.withHandle(h -> h
            .createQuery("INSERT INTO identifiables(created, description, identifiable_type, label, last_modified, iiif_image, uuid) VALUES (:created, :description::JSONB, :type, :label::JSONB, :lastModified, :iiifImage::JSONB, :uuid) RETURNING *")
            .bindBean(identifiable)
            .mapToBean(IdentifiableImpl.class)
            .findOnly());
    return (I) result;
  }

  @Override
  public I update(I identifiable) {
    identifiable.setLastModified(LocalDateTime.now());

    IdentifiableImpl result = null;

    // do not update/left out from statement: created, uuid
    result = dbi.withHandle(h -> h
            .createQuery("UPDATE identifiables SET description=:description::JSONB, identifiable_type=:type, label=:label::JSONB, last_modified=:lastModified, iiif_image=:iiifImage::JSONB WHERE uuid=:uuid RETURNING *")
            .bindBean(identifiable)
            .mapToBean(IdentifiableImpl.class
            )
            .findOnly());
    return (I) result;
  }

  protected Integer selectNextSortIndexForParentChildren(Jdbi dbi, String tableName, String columNameParentUuid, UUID parentUuid) {
    // first child: max gets no results (= null)):
    Integer sortIndex = dbi.withHandle((Handle h) -> h.createQuery("SELECT MAX(sortIndex) + 1 FROM " + tableName + " WHERE " + columNameParentUuid + " = :parent_uuid").bind("parent_uuid", parentUuid).mapTo(Integer.class).findOnly());
    if (sortIndex == null) {
      sortIndex = 0;
    }
    final Integer sortIndexDb = sortIndex;
    return sortIndexDb;
  }
}
