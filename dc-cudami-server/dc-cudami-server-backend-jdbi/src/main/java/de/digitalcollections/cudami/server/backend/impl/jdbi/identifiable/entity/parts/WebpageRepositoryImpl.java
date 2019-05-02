package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl<E extends Entity> extends EntityPartRepositoryImpl<Webpage, E> implements WebpageRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageRepositoryImpl.class);

  @Autowired
  public WebpageRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM webpages";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOnly());
    return count;
  }

  @Override
  public PageResponse<Webpage> find(PageRequest pageRequest) {
    StringBuilder query = new StringBuilder()
        .append("SELECT " + IDENTIFIABLE_COLUMNS + ", text")
        .append(" FROM webpages");

    addPageRequestParams(pageRequest, query);

    List<WebpageImpl> result = dbi.withHandle(h -> h.createQuery(query.toString())
        .mapToBean(WebpageImpl.class)
        .list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Webpage findOne(UUID uuid) {
    StringBuilder query = new StringBuilder()
        .append("SELECT " + IDENTIFIABLE_COLUMNS + ", text")
        .append(" FROM webpages")
        .append(" WHERE uuid = :uuid");

    Webpage webpage = dbi.withHandle(h -> h.createQuery(query.toString())
        .bind("uuid", uuid)
        .mapToBean(WebpageImpl.class)
        .findOnly());
    if (webpage != null) {
      webpage.setChildren(getChildren(webpage));
//      webpage.setIdentifiables(getIdentifiables(webpage));
    }
    return webpage;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"uuid"};
  }

  @Override
  public Webpage save(Webpage webpage) {
    webpage.setUuid(UUID.randomUUID());
    webpage.setCreated(LocalDateTime.now());
    webpage.setLastModified(LocalDateTime.now());

    Webpage result = dbi.withHandle(h -> h
        .createQuery("INSERT INTO webpages(uuid, created, description, identifiable_type, label, last_modified, entity_type, text) VALUES (:uuid, :created, :description::JSONB, :type, :label::JSONB, :lastModified, :entityType, :text::JSONB) RETURNING *")
        .bindBean(webpage)
        .mapToBean(WebpageImpl.class)
        .findOnly());

    return result;
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid) {
    Webpage savedWebpage = save(webpage);

    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "website_webpage", "website_uuid", parentWebsiteUuid);
    dbi.withHandle(h -> h.createUpdate(
        "INSERT INTO website_webpage(website_uuid, webpage_uuid, sortIndex)"
        + " VALUES (:parent_website_uuid, :uuid, :sortIndex)")
        .bind("parent_website_uuid", parentWebsiteUuid)
        .bind("sortIndex", sortIndex)
        .bindBean(savedWebpage)
        .execute());

    return findOne(savedWebpage.getUuid());
  }

  @Override
  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid) {
    Webpage savedWebpage = save(webpage);

    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "webpage_webpage", "parent_webpage_uuid", parentWebpageUuid);
    dbi.withHandle(h -> h.createUpdate(
        "INSERT INTO webpage_webpage(parent_webpage_uuid, child_webpage_uuid, sortIndex)"
        + " VALUES (:parent_webpage_uuid, :uuid, :sortIndex)")
        .bind("parent_webpage_uuid", parentWebpageUuid)
        .bind("sortIndex", sortIndex)
        .bindBean(savedWebpage)
        .execute());

    return findOne(savedWebpage.getUuid());
  }

  @Override
  public Webpage update(Webpage webpage) {
    webpage.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert): uuid, created, identifiable_type, entity_type
    Webpage result = dbi.withHandle(h -> h
        .createQuery("UPDATE webpages SET description=:description::JSONB, label=:label::JSONB, last_modified=:lastModified, text=:text::JSONB WHERE uuid=:uuid RETURNING *")
        .bindBean(webpage)
        .mapToBean(WebpageImpl.class)
        .findOnly());
    return result;
  }

  @Override
  public List<Webpage> getChildren(Webpage webpage) {
    return getChildren(webpage.getUuid());
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String sql = "SELECT " + IDENTIFIABLE_COLUMNS
                 + " FROM webpages INNER JOIN webpage_webpages ww ON uuid = ww.child_webpage_uuid"
                 + " WHERE ww.parent_webpage_uuid = :uuid"
                 + " ORDER BY ww.sortIndex ASC";

//    String query = "SELECT ww.child_webpage_uuid as uuid, i.label as label"
//                   + " FROM webpages wp INNER JOIN webpage_webpage ww ON wp.uuid=ww.parent_webpage_uuid INNER JOIN identifiables i ON ww.child_webpage_uuid=i.uuid"
//                   + " WHERE wp.uuid = :uuid"
//                   + " ORDER BY ww.sortIndex ASC";

    List<WebpageImpl> list = dbi.withHandle(h -> h.createQuery(sql)
        .bind("uuid", uuid)
        .mapToBean(WebpageImpl.class)
        .list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(s -> (Webpage) s).collect(Collectors.toList());
  }
//  @Override
//  public List<Identifiable> getIdentifiables(W webpage) {
//    return getIdentifiables(webpage.getUuid());
//  }
//
//  @Override
//  public List<Identifiable> getIdentifiables(UUID uuid) {
//    // minimal data required for creating text links in a list
//    String query = "SELECT i.uuid as uuid, i.label as label"
//                   + " FROM identifiables i INNER JOIN webpage_identifiables wi ON wi.identifiable_uuid=i.uuid"
//                   + " WHERE wi.webpage_uuid = :uuid"
//                   + " ORDER BY wi.sortIndex ASC";
//
//    List<IdentifiableImpl> list = dbi.withHandle(h -> h.createQuery(query)
//        .bind("uuid", uuid)
//        .mapToBean(IdentifiableImpl.class)
//        .list());
//
//    if (list.isEmpty()) {
//      return new ArrayList<>();
//    }
//    return list.stream().map(Identifiable.class::cast).collect(Collectors.toList());
//  }
//
//  @Override
//  public void addIdentifiable(UUID webpageUuid, UUID identifiableUuid) {
//    Integer sortIndex = selectNextSortIndexForParentChildren(dbi, "webpage_identifiables", "webpage_uuid", webpageUuid);
//    dbi.withHandle(h -> h.createUpdate(
//        "INSERT INTO webpage_identifiables(webpage_uuid, identifiable_uuid, sortIndex)"
//        + " VALUES (:webpage_uuid, :identifiable_uuid, :sortIndex)")
//        .bind("webpage_uuid", webpageUuid)
//        .bind("identifiable_uuid", identifiableUuid)
//        .bind("sortIndex", sortIndex)
//        .execute());
//  }
//
//  @Override
//  public List<Identifiable> saveIdentifiables(W webpage, List<Identifiable> identifiables) {
//    UUID uuid = webpage.getUuid();
//    return saveIdentifiables(uuid, identifiables);
//  }
//
//  @Override
//  public List<Identifiable> saveIdentifiables(UUID identifiablesContainerUuid, List<Identifiable> identifiables) {
//    dbi.withHandle(h -> h.createUpdate("DELETE FROM webpage_identifiables WHERE webpage_uuid = :uuid")
//        .bind("uuid", identifiablesContainerUuid).execute());
//
//    PreparedBatch batch = dbi.withHandle(h -> h.prepareBatch("INSERT INTO webpage_identifiables(webpage_uuid, identifiable_uuid, sortIndex) VALUES(:uuid, :identifiableUuid, :sortIndex)"));
//    for (Identifiable identifiable : identifiables) {
//      batch.bind("uuid", identifiablesContainerUuid)
//          .bind("identifiableUuid", identifiable.getUuid())
//          .bind("sortIndex", identifiables.indexOf(identifiable))
//          .add();
//    }
//    batch.execute();
//    return getIdentifiables(identifiablesContainerUuid);
//  }
}
