package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl<E extends Entity> extends EntityPartRepositoryImpl<Webpage, E>
    implements WebpageRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageRepositoryImpl.class);

  @Autowired
  public WebpageRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM webpages";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Webpage> find(PageRequest pageRequest) {
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, label, description, created, last_modified, text FROM webpages");
    addPageRequestParams(pageRequest, query);

    List<WebpageImpl> result =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(WebpageImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Webpage findOne(UUID uuid) {
    String query =
        "SELECT uuid, label, description, created, last_modified, text FROM webpages WHERE uuid = :uuid";
    Webpage webpage =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(WebpageImpl.class)
                    .findOne()
                    .orElse(null));
    if (webpage != null) {
      webpage.setChildren(getChildren(webpage));
      //      webpage.setIdentifiables(getIdentifiables(webpage));
    }
    return webpage;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"uuid"};
  }

  @Override
  public List<Webpage> getChildren(Webpage webpage) {
    return getChildren(webpage.getUuid());
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String sql =
        "SELECT "
            + "uuid, created, description, label, last_modified"
            + " FROM webpages INNER JOIN webpage_webpages ww ON uuid = ww.child_webpage_uuid"
            + " WHERE ww.parent_webpage_uuid = :uuid"
            + " ORDER BY ww.sortIndex ASC";

    List<WebpageImpl> list =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapToBean(WebpageImpl.class).list());
    return list.stream().map(s -> (Webpage) s).collect(Collectors.toList());
  }

  @Override
  public Webpage getParent(UUID uuid) {
    String query =
        "SELECT uuid, label, description, created, last_modified"
            + " FROM webpages"
            + " INNER JOIN webpage_webpages ww ON uuid = ww.parent_webpage_uuid"
            + " WHERE ww.child_webpage_uuid = :uuid";
    Webpage webpage =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(WebpageImpl.class)
                    .findOne()
                    .orElse(null));
    return webpage;
  }

  @Override
  public Webpage save(Webpage webpage) {
    webpage.setUuid(UUID.randomUUID());
    webpage.setCreated(LocalDateTime.now());
    webpage.setLastModified(LocalDateTime.now());

    String query =
        "INSERT INTO webpages("
            + "uuid, label, description, identifiable_type, created, last_modified, text"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :type, :created, :lastModified, :text::JSONB"
            + ") RETURNING *";
    Webpage result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(webpage)
                    .mapToBean(WebpageImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public Webpage saveWithParentWebpage(Webpage webpage, UUID parentWebpageUuid) {
    Webpage savedWebpage = save(webpage);

    Integer sortIndex =
        selectNextSortIndexForParentChildren(
            dbi, "webpage_webpages", "parent_webpage_uuid", parentWebpageUuid);

    String query =
        "INSERT INTO webpage_webpages(parent_webpage_uuid, child_webpage_uuid, sortIndex)"
            + " VALUES (:parent_webpage_uuid, :uuid, :sortIndex)";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("parent_webpage_uuid", parentWebpageUuid)
                .bind("sortIndex", sortIndex)
                .bindBean(savedWebpage)
                .execute());

    return findOne(savedWebpage.getUuid());
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid) {
    Webpage savedWebpage = save(webpage);

    Integer sortIndex =
        selectNextSortIndexForParentChildren(
            dbi, "website_webpages", "website_uuid", parentWebsiteUuid);
    String query =
        "INSERT INTO website_webpages(website_uuid, webpage_uuid, sortIndex)"
            + " VALUES (:parent_website_uuid, :uuid, :sortIndex)";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("parent_website_uuid", parentWebsiteUuid)
                .bind("sortIndex", sortIndex)
                .bindBean(savedWebpage)
                .execute());

    return findOne(savedWebpage.getUuid());
  }

  @Override
  public Webpage update(Webpage webpage) {
    webpage.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert): uuid, created,
    // identifiable_type, entity_type
    String query =
        "UPDATE webpages SET"
            + " label=:label::JSONB, description=:description::JSONB, last_modified=:lastModified, text=:text::JSONB"
            + " WHERE uuid=:uuid"
            + " RETURNING *";
    Webpage result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(webpage)
                    .mapToBean(WebpageImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }
}
