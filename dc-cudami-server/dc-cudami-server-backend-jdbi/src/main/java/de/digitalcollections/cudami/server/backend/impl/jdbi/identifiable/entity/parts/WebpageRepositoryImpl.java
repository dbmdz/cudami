package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.WebpageRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
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
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl extends EntityPartRepositoryImpl<Webpage>
    implements WebpageRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "wp";
  public static final String TABLE_ALIAS = "w";
  public static final String TABLE_NAME = "webpages";

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return IdentifiableRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".publication_end "
        + mappingPrefix
        + "_publicationEnd, "
        + tableAlias
        + ".publication_start "
        + mappingPrefix
        + "_publicationStart, "
        + tableAlias
        + ".rendering_hints "
        + mappingPrefix
        + "_renderingHints";
  }

  @Autowired
  public WebpageRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi, identifierRepository, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, WebpageImpl.class);
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<Webpage> collections) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Webpage findOne(UUID uuid, Filtering filtering) {
    Webpage webpage = super.findOne(uuid, filtering);

    if (webpage != null) {
      webpage.setChildren(getChildren(webpage));
    }
    return webpage;
  }

  @Override
  public Webpage findOne(Identifier identifier) {
    Webpage webpage = super.findOne(identifier);

    if (webpage != null) {
      webpage.setChildren(getChildren(webpage));
    }
    return webpage;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "label", "lastModified", "publicationEnd", "publicationStart"};
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) {

    List<Node> result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "WITH recursive breadcrumb (uuid,label,parent_uuid,depth)"
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
                    .map(Node.class::cast)
                    .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level webpage, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "SELECT w.uuid as uuid, w.label as label"
                              + "        FROM webpages w"
                              + "        WHERE uuid= :uuid")
                      .bind("uuid", uuid)
                      .registerRowMapper(BeanMapper.factory(NodeImpl.class))
                      .mapTo(NodeImpl.class)
                      .map(Node.class::cast)
                      .list());
    }

    return new BreadcrumbNavigationImpl(result);
  }

  @Override
  public List<Webpage> getChildren(Webpage webpage) {
    return getChildren(webpage.getUuid());
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN webpage_webpages ww ON "
                + tableAlias
                + ".uuid = ww.child_webpage_uuid"
                + " WHERE ww.parent_webpage_uuid = :uuid"
                + " ORDER BY ww.sortIndex ASC");

    List<Webpage> result = retrieveList(sqlReducedFields, innerQuery, Map.of("uuid", uuid));
    return result;
  }

  @Override
  public PageResponse<Webpage> getChildren(UUID uuid, PageRequest pageRequest) {
    String commonSql =
        " FROM "
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

    List<Webpage> result = retrieveList(sqlReducedFields, innerQuery, Map.of("uuid", uuid));

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
  public Webpage getParent(UUID uuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN webpage_webpages ww ON "
                + tableAlias
                + ".uuid = ww.parent_webpage_uuid"
                + " WHERE ww.child_webpage_uuid = :uuid");
    Webpage result = retrieveOne(sqlReducedFields, innerQuery, null, Map.of("uuid", uuid));

    return result;
  }

  @Override
  public List<Webpage> getParents(UUID uuid) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public PageResponse<Webpage> getRootNodes(PageRequest pageRequest) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Website getWebsite(UUID rootWebpageUuid) {
    String query =
        "SELECT uuid, refid, label"
            + " FROM websites"
            + " INNER JOIN website_webpages ww ON uuid = ww.website_uuid"
            + " WHERE ww.webpage_uuid = :uuid";

    Website result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("uuid", rootWebpageUuid)
                    .mapToBean(WebsiteImpl.class)
                    .one());
    return result;
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Webpage save(Webpage webpage) {
    webpage.setUuid(UUID.randomUUID());
    webpage.setCreated(LocalDateTime.now());
    webpage.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        webpage.getPreviewImage() == null ? null : webpage.getPreviewImage().getUuid();

    String query =
        "INSERT INTO "
            + tableName
            + "("
            + "uuid, label, description, previewfileresource, preview_hints, custom_attrs,"
            + " identifiable_type,"
            + " created, last_modified,"
            + " text, publication_start, publication_end,"
            + " rendering_hints"
            + ") VALUES ("
            + ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB, :customAttributes::JSONB,"
            + " :type,"
            + " :created, :lastModified,"
            + " :text::JSONB, :publicationStart, :publicationEnd,"
            + " :renderingHints::JSONB"
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(webpage)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = webpage.getIdentifiers();
    saveIdentifiers(identifiers, webpage);

    Webpage result = findOne(webpage.getUuid());
    return result;
  }

  @Override
  public Webpage saveWithParent(Webpage webpage, UUID parentWebpageUuid) {
    final UUID childUuid = webpage.getUuid() == null ? save(webpage).getUuid() : webpage.getUuid();

    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "webpage_webpages", "parent_webpage_uuid", parentWebpageUuid);

    String query =
        "INSERT INTO webpage_webpages(parent_webpage_uuid, child_webpage_uuid, sortIndex)"
            + " VALUES (:parent_webpage_uuid, :child_webpage_uuid, :sortIndex)";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("parent_webpage_uuid", parentWebpageUuid)
                .bind("child_webpage_uuid", childUuid)
                .bind("sortIndex", nextSortIndex)
                .execute());

    return findOne(childUuid);
  }

  @Override
  public Webpage saveWithParentWebsite(Webpage webpage, UUID parentWebsiteUuid) {
    final UUID webpageUuid =
        webpage.getUuid() == null ? save(webpage).getUuid() : webpage.getUuid();

    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "website_webpages", "website_uuid", parentWebsiteUuid);

    String query =
        "INSERT INTO website_webpages(website_uuid, webpage_uuid, sortIndex)"
            + " VALUES (:parent_website_uuid, :webpage_uuid, :sortIndex)";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("parent_website_uuid", parentWebsiteUuid)
                .bind("webpage_uuid", webpageUuid)
                .bind("sortIndex", nextSortIndex)
                .execute());

    return findOne(webpageUuid);
  }

  @Override
  public Webpage update(Webpage webpage) {
    webpage.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    final UUID previewImageUuid =
        webpage.getPreviewImage() == null ? null : webpage.getPreviewImage().getUuid();

    String query =
        "UPDATE "
            + tableName
            + " SET"
            + " label=:label::JSONB, description=:description::JSONB,"
            + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
            + " last_modified=:lastModified,"
            + " text=:text::JSONB, publication_start=:publicationStart, publication_end=:publicationEnd,"
            + " rendering_hints=:renderingHints::JSONB"
            + " WHERE uuid=:uuid";

    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(webpage)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    identifierRepository.deleteByIdentifiable(webpage);
    Set<Identifier> identifiers = webpage.getIdentifiers();
    saveIdentifiers(identifiers, webpage);

    Webpage result = findOne(webpage.getUuid());
    return result;
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<Webpage> children) {
    if (parentUuid == null || children == null) {
      return false;
    }
    String query =
        "UPDATE webpage_webpages"
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
