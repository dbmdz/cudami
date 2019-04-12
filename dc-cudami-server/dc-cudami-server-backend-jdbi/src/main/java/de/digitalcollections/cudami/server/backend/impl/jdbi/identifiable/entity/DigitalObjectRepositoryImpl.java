package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.DigitalObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.DigitalObjectImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class DigitalObjectRepositoryImpl<D extends DigitalObjectImpl> extends EntityRepositoryImpl<D> implements DigitalObjectRepository<D> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectRepositoryImpl.class);

  private final EntityRepository entityRepository;

  @Autowired
  public DigitalObjectRepositoryImpl(
      @Qualifier("identifiableRepositoryImpl") IdentifiableRepository identifiableRepository,
      @Qualifier("entityRepositoryImpl") EntityRepository entityRepository,
      Jdbi dbi) {
    super(dbi, identifiableRepository);
    this.entityRepository = entityRepository;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM digitalobjects";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public PageResponse<D> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT dio.id as id, i.uuid as uuid, i.label as label, i.description as description")
        .append(" FROM digitalobjects dio INNER JOIN entities e ON dio.uuid=e.uuid INNER JOIN identifiables i ON dio.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);

    List<DigitalObjectImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .mapToBean(DigitalObjectImpl.class)
        .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public D findOne(UUID uuid) {
    String query = "SELECT dio.id as id, i.uuid as uuid, i.label as label, i.description as description"
                   + " FROM digitalobjects dio INNER JOIN entities e ON dio.uuid=e.uuid INNER JOIN identifiables i ON dio.uuid=i.uuid"
                   + " WHERE i.uuid = :uuid";

    D digitalObject = (D) dbi.withHandle(h -> h.createQuery(query)
        .bind("uuid", uuid)
        .mapToBean(DigitalObjectImpl.class)
        .findOnly());
    return digitalObject;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"last_modified"};
  }

  @Override
  public D save(D digitalObject) {
    entityRepository.save(digitalObject);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO digitalobjects(uuid) VALUES (:uuid)")
        .bindBean(digitalObject)
        .execute());
    return findOne(digitalObject.getUuid());
  }

  @Override
  public D update(D digitalObject) {
    entityRepository.update(digitalObject);
    // do not update/left out from statement: created, uuid
    // TODO set digitalObject fields (until now there are no own fields...)
//    dbi.withHandle(h -> h.createUpdate("UPDATE digitalobjects SET ... WHERE uuid=:uuid")
//            .bindBean(digitalObject)
//            .execute());
    return findOne(digitalObject.getUuid());
  }
}
