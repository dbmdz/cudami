package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.impl.paging.PageResponseImpl;
import de.digitalcollections.cudami.model.api.identifiable.entity.Entity;
import de.digitalcollections.cudami.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends EntityImpl> extends AbstractPagingAndSortingRepositoryImpl implements EntityRepository<E> {

  @Autowired
  private Jdbi dbi;

  @Autowired
  private IdentifiableRepositoryImpl identifiableRepository;

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
    StringBuilder query = new StringBuilder("SELECT * FROM entities INNER JOIN identifiables ON entities.uuid=identifiables.uuid");

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
    String query = "SELECT * FROM entities INNER JOIN identifiables ON entities.uuid=identifiables.uuid WHERE entities.uuid = :uuid";
    
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

    EntityImpl result = dbi.withHandle(h -> h
            .createQuery("INSERT INTO entities(entity_type, uuid) VALUES (:entityType, :uuid) RETURNING *")
            .bindBean(entity)
            .mapToBean(EntityImpl.class)
            .findOnly());
    return (E) result;
  }

  @Override
  public E update(E entity) {
    identifiableRepository.update(entity);

    // do not update/left out from statement: created, uuid
    EntityImpl result = dbi.withHandle(h -> h
            .createQuery("UPDATE entities SET entity_type=:entityType WHERE uuid=:uuid RETURNING *")
            .bindBean(entity)
            .mapToBean(EntityImpl.class)
            .findOnly());
    return (E) result;
  }
}
