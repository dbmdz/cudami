package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
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
public class WebsiteRepositoryImpl extends EntityRepositoryImpl<Website>
    implements WebsiteRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteRepositoryImpl.class);

  @Autowired
  public WebsiteRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public long count() {
    String sql = "SELECT count(*) FROM websites";
    long count = dbi.withHandle(h -> h.createQuery(sql).mapTo(Long.class).findOne().get());
    return count;
  }

  @Override
  public PageResponse<Website> find(PageRequest pageRequest) {
    StringBuilder query =
        new StringBuilder(
            "SELECT uuid, label, description, created, last_modified, url, registration_date FROM websites");
    addPageRequestParams(pageRequest, query);

    List<WebsiteImpl> result =
        dbi.withHandle(h -> h.createQuery(query.toString()).mapToBean(WebsiteImpl.class).list());
    long total = count();
    PageResponse pageResponse = new PageResponseImpl(result, pageRequest, total);
    return pageResponse;
  }

  @Override
  public Website findOne(UUID uuid) {
    String query =
        "SELECT uuid, label, description, created, last_modified, url, registration_date FROM websites WHERE uuid = :uuid";
    Website website =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", uuid)
                    .mapToBean(WebsiteImpl.class)
                    .findOne()
                    .orElse(null));
    if (website != null) {
      website.setRootPages(getRootPages(website));
    }
    return website;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"url"};
  }

  @Override
  public Website save(Website website) {
    website.setUuid(UUID.randomUUID());
    website.setCreated(LocalDateTime.now());
    website.setLastModified(LocalDateTime.now());

    String query =
        "INSERT INTO websites("
            + "uuid, label, description, identifiable_type, entity_type, created, last_modified, url, registration_date"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :type, :entityType, :created, :lastModified, :url, :registrationDate"
            + ") RETURNING *";
    Website result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(website)
                    .mapToBean(WebsiteImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public Website update(Website website) {
    website.setLastModified(LocalDateTime.now());

    // do not update/left out from statement (not changed since insert): uuid, created,
    // identifiable_type, entity_type
    String query =
        "UPDATE websites SET"
            + " label=:label::JSONB, description=:description::JSONB, last_modified=:lastModified, url=:url, registration_date=:registrationDate"
            + " WHERE uuid=:uuid"
            + " RETURNING *";
    Website result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bindBean(website)
                    .mapToBean(WebsiteImpl.class)
                    .findOne()
                    .orElse(null));
    return result;
  }

  @Override
  public List<Webpage> getRootPages(Website website) {
    UUID uuid = website.getUuid();
    return getRootPages(uuid);
  }

  @Override
  public List<Webpage> getRootPages(UUID uuid) {
    // minimal data required (= identifiable fields) for creating text links/teasers in a list
    String sql =
        "SELECT "
            + "uuid, created, description, label, last_modified"
            + " FROM webpages INNER JOIN website_webpages ww ON uuid = ww.webpage_uuid"
            + " WHERE ww.website_uuid = :uuid"
            + " ORDER BY ww.sortIndex ASC";

    //    String query = "SELECT ww.webpage_uuid as uuid, i.label as label"
    //                   + " FROM websites ws INNER JOIN website_webpage ww ON
    // ws.uuid=ww.website_uuid INNER JOIN identifiables i ON ww.webpage_uuid=i.uuid"
    //                   + " WHERE ws.uuid = :uuid"
    //                   + " ORDER BY ww.sortIndex ASC";
    List<WebpageImpl> list =
        dbi.withHandle(
            h -> h.createQuery(sql).bind("uuid", uuid).mapToBean(WebpageImpl.class).list());

    if (list.isEmpty()) {
      return new ArrayList<>();
    }
    return list.stream().map(WebpageImpl.class::cast).collect(Collectors.toList());
  }
}
