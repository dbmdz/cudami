package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.core.model.impl.paging.PageResponseImpl;
import de.digitalcollections.cudami.model.api.identifiable.resource.Resource;
import de.digitalcollections.cudami.model.impl.identifiable.resource.ResourceImpl;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ResourceRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import java.util.List;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ResourceRepositoryImpl<R extends ResourceImpl> extends AbstractPagingAndSortingRepositoryImpl implements ResourceRepository<R> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRepositoryImpl.class);

  @Autowired
  private Jdbi dbi;

  @Autowired
  private IdentifiableRepositoryImpl identifiableRepository;

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM resources";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public R create() {
    return (R) new ResourceImpl();
  }

  @Override
  public PageResponse<R> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder("SELECT r.resource_type as resourceType, r.uuid as uuid, i.label as label, i.description as description")
            .append(" FROM resources r INNER JOIN identifiables i ON r.uuid=i.uuid");

    addPageRequestParams(pageRequest, query);
    List<ResourceImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
            .mapToBean(ResourceImpl.class)
            .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public R findOne(UUID uuid) {
    String query = "SELECT r.resource_type as resourceType, r.uuid as uuid, i.label as label, i.description as description"
            + " FROM resources r INNER JOIN identifiables i ON r.uuid=i.uuid"
            + " WHERE r.uuid = :uuid";

    List<? extends Resource> list = dbi.withHandle(h -> h.createQuery(query)
            .bind("uuid", uuid)
            .mapToBean(ResourceImpl.class)
            .list());
    if (list.isEmpty()) {
      return null;
    }
    return (R) list.get(0);
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "resourceType", "lastModified"};
  }

  @Override
  public R save(R resource) {
    identifiableRepository.save(resource);
    dbi.withHandle(h -> h.createUpdate("INSERT INTO resources(resource_type, uuid) VALUES (:resourceType, :uuid)")
            .bindBean(resource)
            .execute());
    return findOne(resource.getUuid());
  }

  @Override
  public R update(R resource) {
    identifiableRepository.update(resource);
    // do not update/left out from statement: created, uuid
    dbi.withHandle(h -> h.createUpdate("UPDATE resources SET resource_type=:resourceType WHERE uuid=:uuid")
            .bindBean(resource)
            .execute());
    return findOne(resource.getUuid());
  }
}
