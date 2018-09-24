package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends Entity> extends IdentifiableRepositoryImpl<E> implements EntityRepository<E> {

  protected final Jdbi dbi;
  private final IdentifiableRepository identifiableRepository;

  @Autowired
  public EntityRepositoryImpl(Jdbi dbi, @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository) {
    this.dbi = dbi;
    this.identifiableRepository = identifiableRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM entities";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public E create() {
    return (E) new EntityImpl();
  }

  @Override
  public PageResponse<E> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT e.entity_type as entityType, e.uuid as uuid, i.label as label, i.description as description")
            .append(" FROM entities e INNER JOIN identifiables i ON e.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);
    List<EntityImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(EntityImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public E findOne(UUID uuid) {
    String query = "SELECT e.entity_type as entityType, e.uuid as uuid, i.label as label, i.description as description"
            + " FROM entities e INNER JOIN identifiables i ON e.uuid=i.uuid"
            + " WHERE e.uuid = :uuid";

    List<? extends Entity> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(EntityImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    return (E) list.get(0);
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "entityType", "lastModified"};
  }

  @Override
  public E save(E entity) {
    identifiableRepository.save(entity);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO entities(entity_type, uuid) VALUES (:entityType, :uuid)")
            .bindBean(entity)
            .execute());
    return findOne(entity.getUuid());
  }

  @Override
  public E update(E entity) {
    identifiableRepository.update(entity);
    // do not update/left out from statement: created, uuid
    dbi.withHandle(h -> h.createUpdate("UPDATE entities SET entity_type=:entityType WHERE uuid=:uuid")
            .bindBean(entity)
            .execute());
    return findOne(entity.getUuid());
  }
}
