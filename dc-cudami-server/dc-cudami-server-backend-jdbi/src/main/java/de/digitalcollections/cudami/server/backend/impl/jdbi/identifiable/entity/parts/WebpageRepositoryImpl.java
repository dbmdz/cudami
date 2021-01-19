package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.impl.identifiable.NodeImpl;
import de.digitalcollections.model.impl.identifiable.entity.WebsiteImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.WebpageImpl;
import de.digitalcollections.model.impl.paging.PageResponseImpl;
import de.digitalcollections.model.impl.view.BreadcrumbNavigationImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl
        extends EntityPartRepositoryImpl<WebpageImpl> implements WebpageRepository<WebpageImpl> {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageRepositoryImpl.class);

  public static final String SQL_REDUCED_FIELDS_WP
          = " w.uuid wp_uuid, w.label wp_label, w.description wp_description,"
          + " w.identifiable_type wp_type,"
          + " w.created wp_created, w.last_modified wp_lastModified,"
          + " w.publication_start wp_publicationStart, w.publication_end wp_publicationEnd,"
          + " w.rendering_hints wp_renderingHints,"
          + " w.previewp_hints wp_previewImageRenderingHints";

  public static final String SQL_FULL_FIELDS_WP = SQL_REDUCED_FIELDS_WP + ", w.text wp_text";

  public static final String TABLE_NAME = "webpages";

  @Autowired
  public WebpageRepositoryImpl(Jdbi dbi,
          IdentifierRepository identifierRepository) {
    super(
            dbi,
            identifierRepository,
            TABLE_NAME,
            "w",
            "wp",
            WebpageImpl.class,
            SQL_REDUCED_FIELDS_WP,
            SQL_FULL_FIELDS_WP);
  }

  @Override
  public WebpageImpl findOne(UUID uuid, Filtering filtering) {
    WebpageImpl webpage = super.findOne(uuid, filtering);

    if (webpage != null) {
      webpage.setChildren(
              Stream.ofNullable(getChildren(webpage))
                      .map(Webpage.class::cast)
                      .collect(Collectors.toList()));
    }
    return webpage;
  }

  @Override
  public WebpageImpl findOne(Identifier identifier) {
    WebpageImpl webpage = super.findOne(identifier);

    if (webpage != null) {
      webpage.setChildren(
              Stream.ofNullable(getChildren(webpage))
                      .map(Webpage.class::cast)
                      .collect(Collectors.toList()));
    }
    return webpage;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "label", "lastModified", "publicationEnd", "publicationStart"};
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) {

    List<NodeImpl> result
            = dbi.withHandle(h
                    -> h.createQuery("WITH recursive breadcrumb (uuid,label,parent_uuid,depth)"
                    + " AS ("
                    + "        SELECT w.uuid as uuid, w.label as label, ww.parent_webpage_uuid as parent_uuid,99 as depth"
                    + "        FROM webpages w, webpage_webpages ww"
                    + "        WHERE uuid= :uuid and ww.child_webpage_uuid = w.uuid"
                    + ""
                    + "        UNION ALL"
                    + "        SELECT w.uuid as uuid, w.label as label, ww.parent_webpage_uuid as parent_uuid, depth-1 as depth"
                    + "        FROM webpages w,"
                    + "             webpage_webpages ww,"
                    + "             breadcrumb b"
                    + "        WHERE b.uuid = ww.child_webpage_uuid and ww.parent_webpage_uuid = w.uuid AND ww.parent_webpage_uuid is not null"
                    + "    )"
                    + " SELECT * from breadcrumb"
                    + " UNION"
                    + " SELECT null as uuid, w.label as label, null as parent_uuid, 0 as depth"
                    + " FROM websites w, website_webpages ww, breadcrumb b"
                    + " WHERE ww.webpage_uuid = b.parent_uuid and w.uuid = ww.website_uuid"
                    + " ORDER BY depth ASC")
                    .bind("uuid", uuid)
                    .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                    .mapTo(NodeImpl.class)
                    .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level webpage, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result
              = dbi.withHandle(h
                      -> h.createQuery("SELECT w.uuid as uuid, w.label as label"
                      + "        FROM webpages w"
                      + "        WHERE uuid= :uuid")
                      .bind("uuid", uuid)
                      .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                      .mapTo(NodeImpl.class)
                      .list());
    }

    List<Node> nodes = result.stream().map(s -> (Node) s).collect(Collectors.toList());

    return new BreadcrumbNavigationImpl(nodes);
  }

  @Override
  public List<WebpageImpl> getChildren(WebpageImpl webpage) {
    return getChildren(webpage.getUuid());
  }

  @Override
  public List<WebpageImpl> getChildren(UUID uuid) {
    StringBuilder innerQuery
            = new StringBuilder("SELECT * FROM "
                    + tableName
                    + " AS "
                    + tableAlias
                    + " INNER JOIN webpage_webpages ww ON "
                    + tableAlias
                    + ".uuid = ww.child_webpage_uuid"
                    + " WHERE ww.parent_webpage_uuid = :uuid"
                    + " ORDER BY ww.sortIndex ASC");

    List<WebpageImpl> result = retrieveList(reducedFieldsSql, innerQuery, Map.of("uuid", uuid));
    return result;
  }

  @Override
  public PageResponse<WebpageImpl> getChildren(UUID uuid, PageRequest pageRequest) {
    String commonSql = " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " INNER JOIN webpage_webpages ww ON "
            + tableAlias
            + ".uuid = ww.child_webpage_uuid"
            + " WHERE ww.parent_webpage_uuid = :uuid";

    StringBuilder innerQuery = new StringBuilder("SELECT *" + commonSql);
    addFiltering(pageRequest, innerQuery);
    if (pageRequest.getSorting() == null) {
      innerQuery.append(" ORDER BY ww.sortIndex ASC");
    }
    addPageRequestParams(pageRequest, innerQuery);

    List<WebpageImpl> result = retrieveList(reducedFieldsSql, innerQuery, Map.of("uuid", uuid));

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery);
    long total = retrieveCount(countQuery, Map.of("uuid", uuid));

    return new PageResponseImpl<>(result, pageRequest, total);
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
      case "publicationEnd":
        return tableAlias + ".publication_end";
      case "publicationStart":
        return tableAlias + ".publication_start";
      default:
        return null;
    }
  }

  @Override
  public WebpageImpl getParent(UUID uuid) {
    StringBuilder innerQuery = new StringBuilder("SELECT * FROM "
            + tableName
            + " AS "
            + tableAlias
            + " INNER JOIN webpage_webpages ww ON "
            + tableAlias
            + ".uuid = ww.parent_webpage_uuid"
            + " WHERE ww.child_webpage_uuid = :uuid");
    WebpageImpl result = retrieveOne(reducedFieldsSql, innerQuery, Map.of("uuid", uuid));

    return result;
  }

  @Override
  public Website getWebsite(UUID rootWebpageUuid) {
    String query
            = "SELECT uuid, refid, label"
            + " FROM websites"
            + " INNER JOIN website_webpages ww ON uuid = ww.website_uuid"
            + " WHERE ww.webpage_uuid = :uuid";

    WebsiteImpl result
            = dbi.withHandle(
                    h
                    -> h.createQuery(query)
                            .bind("uuid", rootWebpageUuid)
                            .mapToBean(WebsiteImpl.class)
                            .one());
    return result;
  }

  @Override
  public WebpageImpl save(WebpageImpl webpage) {
    webpage.setUuid(UUID.randomUUID());
    webpage.setCreated(LocalDateTime.now());
    webpage.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid
            = webpage.getPreviewImage() == null ? null : webpage.getPreviewImage().getUuid();

    String query
            = "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints,"
            + " identifiable_type,"
            + " created, last_modified,"
            + " text, publication_start, publication_end,"
            + " rendering_hints"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB,"
            + " :type,"
            + " :created, :lastModified,"
            + " :text::JSONB, :publicationStart, :publicationEnd,"
            + " :renderingHints::JSONB"
            + ")";

    dbi.withHandle(
            h
            -> h.createUpdate(query)
                    .bind("previewFileResource", previewImageUuid)
                    .bindBean(webpage)
                    .execute());

    // save identifiers
    Set<Identifier> identifiers = webpage.getIdentifiers();
    saveIdentifiers(identifiers, webpage);

    WebpageImpl result = findOne(webpage.getUuid());
    return result;
  }

  @Override
  public WebpageImpl saveWithParentWebpage(WebpageImpl webpage, UUID parentWebpageUuid) {
    final UUID childUuid
            = webpage.getUuid() == null ? save(webpage).getUuid() : webpage.getUuid();

    Integer nextSortIndex
            = retrieveNextSortIndexForParentChildren(
                    dbi, "webpage_webpages", "parent_webpage_uuid", parentWebpageUuid);

    String query
            = "INSERT INTO webpage_webpages(parent_webpage_uuid, child_webpage_uuid, sortIndex)"
            + " VALUES (:parent_webpage_uuid, :child_webpage_uuid, :sortIndex)";
    dbi.withHandle(
            h
            -> h.createUpdate(query)
                    .bind("parent_webpage_uuid", parentWebpageUuid)
                    .bind("child_webpage_uuid", childUuid)
                    .bind("sortIndex", nextSortIndex)
                    .execute());

    return findOne(childUuid);
  }

  @Override
  public WebpageImpl saveWithParentWebsite(WebpageImpl webpage, UUID parentWebsiteUuid) {
    final UUID webpageUuid
            = webpage.getUuid() == null ? save(webpage).getUuid() : webpage.getUuid();

    Integer nextSortIndex
            = retrieveNextSortIndexForParentChildren(
                    dbi, "website_webpages", "website_uuid", parentWebsiteUuid);

    String query
            = "INSERT INTO website_webpages(website_uuid, webpage_uuid, sortIndex)"
            + " VALUES (:parent_website_uuid, :webpage_uuid, :sortIndex)";
    dbi.withHandle(
            h
            -> h.createUpdate(query)
                    .bind("parent_website_uuid", parentWebsiteUuid)
                    .bind("webpage_uuid", webpageUuid)
                    .bind("sortIndex", nextSortIndex)
                    .execute());

    return findOne(webpageUuid);
  }

  @Override
  public WebpageImpl update(WebpageImpl webpage) {
    webpage.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    final UUID previewImageUuid
            = webpage.getPreviewImage() == null ? null : webpage.getPreviewImage().getUuid();

    String query
            = "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, publication_start=:publicationStart, publication_end=:publicationEnd,"
            + " rendering_hints=:renderingHints::JSONB"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
            h
            -> h.createUpdate(query)
                    .bind("previewFileResource", previewImageUuid)
                    .bindBean(webpage)
                    .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(webpage);
    Set<Identifier> identifiers = webpage.getIdentifiers();
    saveIdentifiers(identifiers, webpage);

    WebpageImpl result = findOne(webpage.getUuid());
    return result;
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<WebpageImpl> children) {
    if (parentUuid == null || children == null) {
      return false;
    }
    String query
            = "UPDATE webpage_webpages"
            + " SET sortindex = :idx"
            + " WHERE child_webpage_uuid = :childWebpageUuid AND parent_webpage_uuid = :parentWebpageUuid;";
    dbi.withHandle(
            h -> {
              PreparedBatch batch = h.prepareBatch(query);
              int idx = 0;
              for (Webpage webpage : children) {
                batch
                        .bind("idx", idx++)
                        .bind("childWebpageUuid", webpage.getUuid())
                        .bind("parentWebpageUuid", parentUuid)
                        .add();
              }
              return batch.execute();
            });
    return true;
  }
}
