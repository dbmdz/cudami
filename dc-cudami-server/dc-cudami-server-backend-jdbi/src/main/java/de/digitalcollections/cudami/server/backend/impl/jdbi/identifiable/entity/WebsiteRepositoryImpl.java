package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.WebsiteRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts.WebpageRepositoryImpl;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebsiteRepositoryImpl extends EntityRepositoryImpl<Website>
    implements WebsiteRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebsiteRepositoryImpl.class);
  public static final String MAPPING_PREFIX = "ws";

  public static final String SQL_REDUCED_FIELDS_WS =
      " w.uuid ws_uuid, w.refid ws_refId, w.label ws_label, w.description ws_description,"
          + " w.identifiable_type ws_type, w.entity_type ws_entityType,"
          + " w.created ws_created, w.last_modified ws_lastModified,"
          + " w.url ws_url, w.registration_date ws_registrationDate,"
          + " w.preview_hints ws_previewImageRenderingHints";

  public static final String SQL_FULL_FIELDS_WS = SQL_REDUCED_FIELDS_WS;

  public static final String TABLE_ALIAS = "w";
  public static final String TABLE_NAME = "websites";

  private final WebpageRepositoryImpl webpageRepositoryImpl;

  @Autowired
  public WebsiteRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      WebpageRepositoryImpl webpageRepositoryImpl) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        WebsiteImpl.class,
        SQL_REDUCED_FIELDS_WS,
        SQL_FULL_FIELDS_WS);
    this.webpageRepositoryImpl = webpageRepositoryImpl;
  }

  @Override
  public Website findOne(UUID uuid, Filtering filtering) {
    Website website = super.findOne(uuid, filtering);

    if (website != null) {
      website.setRootPages(getRootPages(website));
    }
    return website;
  }

  @Override
  public Website findOne(Identifier identifier) {
    Website website = super.findOne(identifier);

    if (website != null) {
      website.setRootPages(getRootPages(website));
    }
    return website;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "refId", "url"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "refId":
        return tableAlias + ".refid";
      case "url":
        return tableAlias + ".url";
      default:
        return null;
    }
  }

  @Override
  public List<Locale> getLanguages() {
    String query =
        "SELECT DISTINCT languages FROM websites as w, jsonb_object_keys(w.label) as languages";
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }
  
  @Override
  public List<Webpage> getRootPages(UUID uuid) {
    final String wpTableAlias = webpageRepositoryImpl.getTableAlias();
    final String wpTableName = webpageRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + wpTableName
                + " AS "
                + wpTableAlias
                + " INNER JOIN website_webpages ww ON "
                + wpTableAlias
                + ".uuid = ww.webpage_uuid"
                + " WHERE ww.website_uuid = :uuid");

    List<Webpage> result =
        webpageRepositoryImpl.retrieveList(
            WebpageRepositoryImpl.SQL_REDUCED_FIELDS_WP, innerQuery, Map.of("uuid", uuid));
    return result;
  }

  @Override
  public PageResponse<Webpage> getRootPages(UUID uuid, PageRequest pageRequest) {
    final String wpTableAlias = webpageRepositoryImpl.getTableAlias();
    final String wpTableName = webpageRepositoryImpl.getTableName();

    String commonSql =
        " FROM "
            + wpTableName
            + " AS "
            + wpTableAlias
            + " INNER JOIN website_webpages ww ON "
            + wpTableAlias
            + ".uuid = ww.webpage_uuid"
            + " WHERE ww.website_uuid = :uuid";

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    if (pageRequest.getSorting() == null) {
      innerQuery.append(" ORDER BY ww.sortIndex ASC");
    }
    addPageRequestParams(pageRequest, innerQuery);

    List<Webpage> result =
        webpageRepositoryImpl.retrieveList(
            WebpageRepositoryImpl.SQL_REDUCED_FIELDS_WP, innerQuery, Map.of("uuid", uuid));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery);
    long total = retrieveCount(countQuery, Map.of("uuid", uuid));

    return new PageResponseImpl<>(result, pageRequest, total);
  }

  @Override
  public Website save(Website website) {
    website.setUuid(UUID.randomUUID());
    website.setCreated(LocalDateTime.now());
    website.setLastModified(LocalDateTime.now());
    // refid is generated as serial, DO NOT SET!
    final UUID previewImageUuid =
        website.getPreviewImage() == null ? null : website.getPreviewImage().getUuid();

    String query =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type, entity_type,"
            + " created, last_modified,"
            + " url, registration_date"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type, :entityType,"
            + " :created, :lastModified,"
            + " :url, :registrationDate"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(website)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = website.getIdentifiers();
    saveIdentifiers(identifiers, website);

    Website result = findOne(website.getUuid());
    return result;
  }

  @Override
  public Website update(Website website) {
    website.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    final UUID previewImageUuid =
        website.getPreviewImage() == null ? null : website.getPreviewImage().getUuid();

    String query =
        "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " url=:url, registration_date=:registrationDate"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(website)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(website);
    Set<Identifier> identifiers = website.getIdentifiers();
    saveIdentifiers(identifiers, website);

    Website result = findOne(website.getUuid());
    return result;
  }

  @Override
  public boolean updateRootPagesOrder(UUID websiteUuid, List<Webpage> rootPages) {
    if (websiteUuid == null || rootPages == null) {
      return false;
    }
    String query =
        "UPDATE website_webpages"
            + " SET sortindex = :idx"
            + " WHERE website_uuid = :websiteUuid AND webpage_uuid = :webpageUuid;";
    dbi.withHandle(
        h -> {
          PreparedBatch batch = h.prepareBatch(query);
          int idx = 0;
          for (Webpage webpage : rootPages) {
            batch
                .bind("idx", idx++)
                .bind("webpageUuid", webpage.getUuid())
                .bind("websiteUuid", websiteUuid)
                .add();
          }
          return batch.execute();
        });
    return true;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "lastModified", "refId", "url"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return "w.created";
      case "lastModified":
        return "w.last_modified";
      case "refId":
        return "w.refid";
      case "url":
        return "w.url";
      default:
        return null;
    }
  }
}
