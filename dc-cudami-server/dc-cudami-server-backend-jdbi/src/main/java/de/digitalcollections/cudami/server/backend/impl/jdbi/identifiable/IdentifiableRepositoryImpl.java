package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractPagingAndSortingRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.IdentifiableImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class IdentifiableRepositoryImpl<I extends Identifiable> extends AbstractPagingAndSortingRepositoryImpl implements IdentifiableRepository<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableRepositoryImpl.class);

  protected static final String IDENTIFIABLE_COLUMNS = "uuid, created, description, label, last_modified";

  protected Jdbi dbi;

  @Autowired
  public IdentifiableRepositoryImpl(Jdbi dbi) {
    this.dbi = dbi;
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM identifiables";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
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
  public List<I> find(String searchTerm, int maxResults) {
    // TODO: think about using GIN
    // see: https://bitnine.net/blog-postgresql/postgresql-internals-jsonb-type-and-its-indexes/
    // see: https://www.postgresql.org/docs/10/datatype-json.html
    StringBuilder query = new StringBuilder("WITH flattened AS (SELECT uuid, label, description, identifiable_type, jsonb_array_elements(label#>'{translations}')->>'text' AS text FROM identifiables)");
    query.append(" SELECT uuid, label, description, identifiable_type FROM flattened WHERE text ILIKE '%' || :searchTerm || '%'");
    query.append(" LIMIT :maxResults");

    List<IdentifiableImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
      .bind("searchTerm", searchTerm)
      .bind("maxResults", maxResults)
      .mapToBean(IdentifiableImpl.class)
      .list());
    List<I> identifiables = convertToGenericList(result);
    return identifiables;
  }

  protected List<I> convertToGenericList(List<IdentifiableImpl> identifiables) {
    if (identifiables == null) {
      return null;
    }
    List<I> genericContent = identifiables.stream().map(s -> (I) s).collect(Collectors.toList());
    return genericContent;
  }

  @Override
  public I findOne(UUID uuid) {
    I identifiable = (I) dbi.withHandle(h -> h.createQuery(
      "SELECT * FROM identifiables WHERE uuid = :uuid")
      .bind("uuid", uuid)
      .mapToBean(IdentifiableImpl.class)
      .findOne().orElse(null));
    return identifiable;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "type", "last_modified"};
  }

  @Override
  public I save(I identifiable) {
    throw new UnsupportedOperationException("use save of specific/inherited identifiable repository");
//    identifiable.setUuid(UUID.randomUUID());
//    identifiable.setCreated(LocalDateTime.now());
//    identifiable.setLastModified(LocalDateTime.now());
//
//    IdentifiableImpl result = dbi.withHandle(h -> h
//        .createQuery("INSERT INTO identifiables(created, description, identifiable_type, label, last_modified, uuid) VALUES (:created, :description::JSONB, :type, :label::JSONB, :lastModified, :uuid) RETURNING *")
//        .bindBean(identifiable)
//        .mapToBean(IdentifiableImpl.class)
//        .findOnly());
//    return (I) result;
  }

  @Override
  public I update(I identifiable) {
    throw new UnsupportedOperationException("use update of specific/inherited identifiable repository");
//    identifiable.setLastModified(LocalDateTime.now());
//
//    // do not update/left out from statement: created, uuid
//    IdentifiableImpl result = dbi.withHandle(h -> h
//        .createQuery("UPDATE identifiables SET description=:description::JSONB, identifiable_type=:type, label=:label::JSONB, last_modified=:lastModified WHERE uuid=:uuid RETURNING *")
//        .bindBean(identifiable)
//        .mapToBean(IdentifiableImpl.class)
//        .findOnly());
//    return (I) result;
  }

  protected Integer selectNextSortIndexForParentChildren(Jdbi dbi, String tableName, String columNameParentUuid, UUID parentUuid) {
    // first child: max gets no results (= null)):
    Integer sortIndex = dbi.withHandle((Handle h) -> h
      .createQuery("SELECT MAX(sortIndex) + 1 FROM " + tableName + " WHERE " + columNameParentUuid + " = :parent_uuid")
      .bind("parent_uuid", parentUuid)
      .mapTo(Integer.class)
      .findOne().orElse(null));
    if (sortIndex == null) {
      sortIndex = 0;
    }
    final Integer sortIndexDb = sortIndex;
    return sortIndexDb;
  }

  protected int getIndex(LinkedHashSet<? extends Identifiable> list, Identifiable identifiable) {
    boolean found = false;
    int pos = -1;
    for (Iterator iterator = list.iterator(); iterator.hasNext();) {
      pos = pos + 1;
      Identifiable idf = (Identifiable) iterator.next();
      if (idf.getUuid().equals(identifiable.getUuid())) {
        found = true;
        break;
      }
    }
    if (found) {
      return pos;
    }
    return -1;
  }
}
