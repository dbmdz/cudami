package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.web;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts.EntityPartRepositoryImpl;
import de.digitalcollections.model.api.filter.FilterValuePlaceholder;
import de.digitalcollections.model.api.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.api.identifiable.entity.Website;
import de.digitalcollections.model.api.identifiable.entity.parts.Webpage;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import de.digitalcollections.model.api.view.BreadcrumbNavigation;
import de.digitalcollections.model.identifiable.Node;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.identifiable.web.BreadcrumbNavigation;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

  public static String getSqlInsertFields() {
    return IdentifiableRepositoryImpl.getSqlInsertFields()
        + ", publication_end, publication_start, rendering_hints, text";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return IdentifiableRepositoryImpl.getSqlInsertValues()
        + ", :publicationEnd, :publicationStart, :renderingHints::JSONB, :text::JSONB";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return IdentifiableRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
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

  public static String getSqlUpdateFieldValues() {
    return IdentifiableRepositoryImpl.getSqlUpdateFieldValues()
        + ", publication_end=:publicationEnd, publication_start=:publicationStart, rendering_hints=:renderingHints::JSONB, text=:text::JSONB";
  }

  @Autowired
  public WebpageRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Webpage.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
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
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("publicationEnd", "publicationStart"));
    return allowedOrderByFields;
  }

  @Override
  public BreadcrumbNavigation getBreadcrumbNavigation(UUID uuid) {

    List<Node> result =
        dbi.withHandle(h ->
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
                    .registerRowMapper(BeanMapper.factory(Node.class))
                    .mapTo(Node.class)
                    .map(Node.class::cast)
                    .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level webpage, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result =
          dbi.withHandle(h ->
                  h.createQuery(
                          "SELECT w.uuid as uuid, w.label as label"
                              + "        FROM webpages w"
                              + "        WHERE uuid= :uuid")
                      .bind("uuid", uuid)
                      .registerRowMapper(BeanMapper.factory(Node.class))
                      .mapTo(Node.class)
                      .map(Node.class::cast)
                      .list());
    }

    return new BreadcrumbNavigation(result);
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

    List<Webpage> result =
        retrieveList(sqlSelectReducedFields, innerQuery, Map.of("uuid", uuid), null);
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

    List<Webpage> result =
        retrieveList(sqlSelectReducedFields, innerQuery, Map.of("uuid", uuid), null);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery);
    long total = retrieveCount(countQuery, Map.of("uuid", uuid));

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    if (super.getColumnName(modelProperty) != null) {
      return super.getColumnName(modelProperty);
    }
    switch (modelProperty) {
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
    String sqlAdditionalJoins =
        " INNER JOIN webpage_webpages ww ON " + tableAlias + ".uuid = ww.parent_webpage_uuid";

    Filtering filtering =
        Filtering.defaultBuilder()
            .filter("ww.child_webpage_uuid")
            .isEquals(new FilterValuePlaceholder(":uuid"))
            .build();

    Webpage result =
        retrieveOne(sqlSelectReducedFields, sqlAdditionalJoins, filtering, Map.of("uuid", uuid));

    return result;
  }

  @Override
  public List<Webpage> getParents(UUID uuid) {
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

    List<Webpage> result =
        retrieveList(sqlSelectReducedFields, innerQuery, Map.of("uuid", uuid), null);
    return result;
  }

  @Override
  public PageResponse<Webpage> getRootNodes(PageRequest pageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE NOT EXISTS (SELECT FROM webpage_webpages WHERE child_webpage_uuid = "
            + tableAlias
            + ".uuid)";
    return find(pageRequest, commonSql, null);
  }

  @Override
  public List<Locale> getRootNodesLanguages() {
    String query =
        "SELECT DISTINCT languages"
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + ", jsonb_object_keys("
            + tableAlias
            + ".label) AS languages"
            + " WHERE NOT EXISTS (SELECT FROM webpage_webpages WHERE child_webpage_uuid = "
            + tableAlias
            + ".uuid)";
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  public Website getWebsite(UUID rootWebpageUuid) {
    String query =
        "SELECT uuid, refid, label"
            + " FROM websites"
            + " INNER JOIN website_webpages ww ON uuid = ww.website_uuid"
            + " WHERE ww.webpage_uuid = :uuid";

    Website result =
        dbi.withHandle(h ->
                h.createQuery(query)
                    .bind("uuid", rootWebpageUuid)
                    .mapToBean(Website.class)
                    .one());
    return result;
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Webpage save(Webpage webpage) {
    super.save(webpage);
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
    super.update(webpage);
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
