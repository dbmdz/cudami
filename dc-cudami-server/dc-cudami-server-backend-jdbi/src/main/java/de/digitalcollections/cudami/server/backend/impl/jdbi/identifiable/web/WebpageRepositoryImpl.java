package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.web;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class WebpageRepositoryImpl extends IdentifiableRepositoryImpl<Webpage>
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
  public WebpageRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Webpage.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues(),
        cudamiConfig.getOffsetForAlternativePaging());
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<UUID> childrenUuids) {
    if (parentUuid == null || childrenUuids == null) {
      return false;
    }
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "webpage_webpages", "parent_webpage_uuid", parentUuid);

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO webpage_webpages(parent_webpage_uuid, child_webpage_uuid, sortIndex)"
                      + " VALUES (:parentWebpageUuid, :childWebpageUuid, :sortIndex) ON CONFLICT (parent_webpage_uuid, child_webpage_uuid) DO NOTHING");
          childrenUuids.forEach(
              childUuid -> {
                preparedBatch
                    .bind("parentWebpageUuid", parentUuid)
                    .bind("childWebpageUuid", childUuid)
                    .bind("sortIndex", nextSortIndex + getIndex(childrenUuids, childUuid))
                    .add();
              });
          preparedBatch.execute();
        });
    return true;
  }

  @Override
  public SearchPageResponse<Webpage> findChildren(UUID uuid, SearchPageRequest searchPageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " INNER JOIN webpage_webpages cc ON "
            + tableAlias
            + ".uuid = cc.child_webpage_uuid"
            + " WHERE cc.parent_webpage_uuid = :uuid";
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    String searchTerm = searchPageRequest.getQuery();
    if (StringUtils.hasText(searchTerm)) {
      commonSql += " AND " + getCommonSearchSql(tableAlias);
      argumentMappings.put("searchTerm", this.escapeTermForJsonpath(searchTerm));
    }

    StringBuilder innerQuery = new StringBuilder("SELECT cc.sortindex AS idx, *" + commonSql);
    addFiltering(searchPageRequest, innerQuery, argumentMappings);

    String orderBy = getOrderBy(searchPageRequest.getSorting());
    if (!StringUtils.hasText(orderBy)) {
      orderBy = "ORDER BY idx ASC";
      innerQuery.append(
          " ORDER BY cc.sortindex"); // must be the column itself to use window functions
    }
    addPageRequestParams(searchPageRequest, innerQuery);

    List<Webpage> result =
        retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery =
        new StringBuilder("SELECT count(" + tableAlias + ".uuid)" + commonSql);
    addFiltering(searchPageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponse<>(result, searchPageRequest, total);
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
    List<BreadcrumbNode> result =
        dbi.withHandle(
            h ->
                h.createQuery(
                        "WITH RECURSIVE breadcrumb (targetId, label, parentId, depth)"
                            + " AS ("
                            + "        SELECT w.uuid AS targetId, w.label AS label, ww.parent_webpage_uuid AS parentId, 99 AS depth"
                            + "        FROM webpages w, webpage_webpages ww"
                            + "        WHERE uuid=:uuid and ww.child_webpage_uuid = w.uuid"
                            + ""
                            + "        UNION ALL"
                            + "        SELECT w.uuid AS targetId, w.label AS label, ww.parent_webpage_uuid AS parentId, depth-1 AS depth"
                            + "        FROM webpages w, webpage_webpages ww, breadcrumb b"
                            + "        WHERE b.targetId = ww.child_webpage_uuid AND ww.parent_webpage_uuid = w.uuid AND ww.parent_webpage_uuid IS NOT NULL"
                            + "    )"
                            + ""
                            + " SELECT cast(targetId AS VARCHAR) AS targetId, label, parentId, depth FROM breadcrumb"
                            + ""
                            + " UNION"
                            + " SELECT NULL AS targetId, ws.label AS label, NULL AS parentId, 0 AS depth"
                            + " FROM websites ws, website_webpages ww, breadcrumb b"
                            + " WHERE ww.webpage_uuid = b.parentId AND ws.uuid = ww.website_uuid"
                            + ""
                            + " ORDER BY depth ASC")
                    .bind("uuid", uuid)
                    .mapTo(BreadcrumbNode.class)
                    .list());

    if (result.isEmpty()) {
      // Special case: If we are on a top level webpage, we have no parent, so
      // we must construct a breadcrumb more or less manually
      result =
          dbi.withHandle(
              h ->
                  h.createQuery(
                          "SELECT cast(w.uuid AS VARCHAR) as targetId, w.label as label"
                              + "        FROM webpages w"
                              + "        WHERE uuid= :uuid")
                      .bind("uuid", uuid)
                      .mapTo(BreadcrumbNode.class)
                      .list());
    }

    return new BreadcrumbNavigation(result);
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT ww.sortindex AS idx, * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN webpage_webpages ww ON "
                + tableAlias
                + ".uuid = ww.child_webpage_uuid"
                + " WHERE ww.parent_webpage_uuid = :uuid"
                + " ORDER BY idx ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    List<Webpage> result =
        retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, "ORDER BY idx ASC");
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
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    StringBuilder innerQuery = new StringBuilder("SELECT ww.sortindex AS idx, *" + commonSql);
    addFiltering(pageRequest, innerQuery, argumentMappings);

    String orderBy = null;
    if (pageRequest.getSorting() == null) {
      orderBy = "ORDER BY idx ASC";
      innerQuery.append(
          " ORDER BY ww.sortindex"); // must be the column itself to use window functions
    }
    addPageRequestParams(pageRequest, innerQuery);

    List<Webpage> result =
        retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(pageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

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
        Filtering.defaultBuilder().filterNative("ww.child_webpage_uuid").isEquals(uuid).build();

    Webpage result = retrieveOne(sqlSelectReducedFields, sqlAdditionalJoins, filtering);

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
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    List<Webpage> result = retrieveList(sqlSelectReducedFields, innerQuery, argumentMappings, null);
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
    return find(pageRequest, commonSql, new HashMap<>());
  }

  @Override
  public SearchPageResponse<Webpage> findRootNodes(SearchPageRequest searchPageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE ("
            + " NOT EXISTS (SELECT FROM webpage_webpages WHERE child_webpage_uuid = "
            + tableAlias
            + ".uuid))";

    String searchTerm = searchPageRequest.getQuery();
    if (!StringUtils.hasText(searchTerm)) {
      return find(searchPageRequest, commonSql, Collections.EMPTY_MAP);
    }
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("searchTerm", this.escapeTermForJsonpath(searchTerm));
    commonSql += " AND " + getCommonSearchSql(tableAlias);
    return find(searchPageRequest, commonSql, argumentMappings);
  }

  @Override
  public SearchPageResponse<Webpage> findRootPagesForWebsite(
      UUID uuid, SearchPageRequest searchPageRequest) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " LEFT JOIN website_webpages ww ON "
            + tableAlias
            + ".uuid = ww.webpage_uuid"
            + " WHERE ww.website_uuid = :uuid";
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", uuid);

    String searchTerm = searchPageRequest.getQuery();
    if (StringUtils.hasText(searchTerm)) {
      commonSql += " AND " + getCommonSearchSql(tableAlias);
      argumentMappings.put("searchTerm", this.escapeTermForJsonpath(searchTerm));
    }

    StringBuilder innerQuery = new StringBuilder("SELECT ww.sortindex AS idx, *" + commonSql);
    addFiltering(searchPageRequest, innerQuery, argumentMappings);

    String orderBy = null;
    if (searchPageRequest.getSorting() == null) {
      orderBy = "ORDER BY idx ASC";
      innerQuery.append(
          " ORDER BY ww.sortindex"); // must be the column itself to use window functions
    }
    addPageRequestParams(searchPageRequest, innerQuery);

    List<Webpage> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    addFiltering(searchPageRequest, countQuery, argumentMappings);
    long total = retrieveCount(countQuery, argumentMappings);

    return new SearchPageResponse<>(result, searchPageRequest, total);
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
        "SELECT uuid, refid, label, url"
            + " FROM websites"
            + " INNER JOIN website_webpages ww ON uuid = ww.website_uuid"
            + " WHERE ww.webpage_uuid = :uuid";

    Website result =
        dbi.withHandle(
            h -> h.createQuery(query).bind("uuid", rootWebpageUuid).mapToBean(Website.class).one());
    return result;
  }

  @Override
  public boolean removeChild(UUID parentUuid, UUID childUuid) {
    if (parentUuid == null || childUuid == null) {
      return false;
    }
    final String sql =
        "DELETE FROM webpage_webpages WHERE parent_webpage_uuid=:parentWebpageUuid AND child_webpage_uuid=:childWebpageUuid";

    dbi.withHandle(
        h ->
            h.createUpdate(sql)
                .bind("parentWebpageUuid", parentUuid)
                .bind("childWebpageUuid", childUuid)
                .execute());
    return true;
  }

  @Override
  public Webpage save(Webpage webpage) {
    super.save(webpage);
    Webpage result = findOne(webpage.getUuid());
    return result;
  }

  @Override
  public Webpage saveWithParent(UUID childWebpageUuid, UUID parentWebpageUuid) {
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
                .bind("child_webpage_uuid", childWebpageUuid)
                .bind("sortIndex", nextSortIndex)
                .execute());

    return findOne(childWebpageUuid);
  }

  @Override
  public Webpage saveWithParentWebsite(UUID webpageUuid, UUID parentWebsiteUuid) {
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
