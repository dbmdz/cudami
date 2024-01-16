package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.web;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.view.BreadcrumbNavigation;
import de.digitalcollections.model.view.BreadcrumbNode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class WebpageRepositoryImpl extends IdentifiableRepositoryImpl<Webpage>
    implements WebpageRepository {

  private static final Logger LOGGER = LoggerFactory.getLogger(WebpageRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "webp";
  public static final String TABLE_ALIAS = "webp";
  public static final String TABLE_NAME = "webpages";

  public WebpageRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Webpage.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public boolean addChildren(UUID parentUuid, List<UUID> childrenUuids) throws RepositoryException {
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
                    .bind("sortIndex", nextSortIndex + childrenUuids.indexOf(childUuid))
                    .add();
              });
          preparedBatch.execute();
        });
    return true;
  }

  @Override
  public Webpage create() throws RepositoryException {
    return new Webpage();
  }

  @Override
  public PageResponse<Webpage> findChildren(UUID uuid, PageRequest pageRequest)
      throws RepositoryException {
    final String crossTableAlias = "xtable";

    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + tableName
                + " AS "
                + tableAlias
                + " INNER JOIN webpage_webpages AS "
                + crossTableAlias
                + " ON "
                + tableAlias
                + ".uuid = "
                + crossTableAlias
                + ".child_webpage_uuid"
                + " WHERE "
                + crossTableAlias
                + ".parent_webpage_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", uuid);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePagingAndSorting(pageRequest, innerQuery, crossTableAlias);
    List<Webpage> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery =
        new StringBuilder("SELECT count(" + tableAlias + ".uuid)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
  }

  @Override
  public PageResponse<Webpage> findRootNodes(PageRequest pageRequest) throws RepositoryException {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE NOT EXISTS (SELECT FROM webpage_webpages WHERE child_webpage_uuid = "
            + tableAlias
            + ".uuid)";
    return find(pageRequest, commonSql);
  }

  @Override
  public PageResponse<Webpage> findRootWebpagesForWebsite(UUID uuid, PageRequest pageRequest)
      throws RepositoryException {
    final String crossTableAlias = "xtable";

    StringBuilder commonSql =
        new StringBuilder(
            " FROM "
                + tableName
                + " AS "
                + tableAlias
                + " LEFT JOIN website_webpages AS "
                + crossTableAlias
                + " ON "
                + tableAlias
                + ".uuid = "
                + crossTableAlias
                + ".webpage_uuid"
                + " WHERE "
                + crossTableAlias
                + ".website_uuid = :uuid");
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", uuid);
    addFiltering(pageRequest, commonSql, argumentMappings);

    StringBuilder innerQuery =
        new StringBuilder("SELECT " + crossTableAlias + ".sortindex AS idx, * " + commonSql);
    String orderBy = addCrossTablePagingAndSorting(pageRequest, innerQuery, crossTableAlias);
    List<Webpage> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, orderBy);

    StringBuilder countQuery = new StringBuilder("SELECT count(*)" + commonSql);
    long total = retrieveCount(countQuery, argumentMappings);

    return new PageResponse<>(result, pageRequest, total);
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
  public Webpage getByIdentifier(Identifier identifier) throws RepositoryException {
    Webpage webpage = super.getByIdentifier(identifier);

    if (webpage != null) {
      webpage.setChildren(getChildren(webpage));
    }
    return webpage;
  }

  @Override
  public List<Webpage> getByUuidsAndFiltering(List<UUID> uuids, Filtering filtering)
      throws RepositoryException {
    List<Webpage> webpages = super.getByUuidsAndFiltering(uuids, filtering);

    Optional.ofNullable(webpages)
        .map(List::parallelStream)
        .ifPresent(
            stream ->
                stream.forEach(
                    webpage -> {
                      try {
                        webpage.setChildren(getChildren(webpage.getUuid()));
                      } catch (RepositoryException e) {
                        LOGGER.error(
                            "Cannot get children of webpage with UUID %s: %s"
                                .formatted(webpage.getUuid(), e),
                            e);
                      }
                    }));
    return webpages;
  }

  @Override
  public List<Webpage> getChildren(UUID uuid) throws RepositoryException {
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
    Map<String, Object> argumentMappings = new HashMap<>(0);
    argumentMappings.put("uuid", uuid);

    List<Webpage> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, "ORDER BY idx ASC");
    return result;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "publicationEnd":
        return tableAlias + ".publication_end";
      case "publicationStart":
        return tableAlias + ".publication_start";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public Webpage getParent(UUID uuid) throws RepositoryException {
    String sqlAdditionalJoins =
        " INNER JOIN webpage_webpages ww ON " + tableAlias + ".uuid = ww.parent_webpage_uuid";

    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.nativeBuilder()
                    .withExpression("ww.child_webpage_uuid")
                    .isEquals(uuid)
                    .build())
            .build();

    Webpage result = retrieveOne(getSqlSelectReducedFields(), filtering, sqlAdditionalJoins);

    return result;
  }

  @Override
  public List<Webpage> getParents(UUID uuid) throws RepositoryException {
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

    List<Webpage> result =
        retrieveList(getSqlSelectReducedFields(), innerQuery, argumentMappings, null);
    return result;
  }

  @Override
  public List<Locale> getRootNodesLanguages() {
    String query =
        "SELECT DISTINCT jsonb_object_keys("
            + tableAlias
            + ".label) AS languages"
            + " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " WHERE NOT EXISTS (SELECT FROM webpage_webpages WHERE child_webpage_uuid = "
            + tableAlias
            + ".uuid)";
    List<Locale> result = dbi.withHandle(h -> h.createQuery(query).mapTo(Locale.class).list());
    return result;
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", publication_end, publication_start, rendering_hints, text";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues()
        + ", :publicationEnd, :publicationStart, :renderingHints::JSONB, :text::JSONB";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectAllFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".text "
        + mappingPrefix
        + "_text";
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
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

  @Override
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", publication_end=:publicationEnd, publication_start=:publicationStart, rendering_hints=:renderingHints::JSONB, text=:text::JSONB";
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
  public Webpage saveParentRelation(UUID childWebpageUuid, UUID parentWebpageUuid)
      throws RepositoryException {
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

    return getByUuid(childWebpageUuid);
  }

  @Override
  public Webpage saveWithParentWebsite(UUID webpageUuid, UUID parentWebsiteUuid)
      throws RepositoryException {
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

    return getByUuid(webpageUuid);
  }

  @Override
  public boolean updateChildrenOrder(UUID parentUuid, List<UUID> children) {
    if (parentUuid == null || children == null || children.isEmpty()) {
      throw new IllegalArgumentException("update failed: given objects must not be null");
    }
    String query =
        "UPDATE webpage_webpages"
            + " SET sortindex = :idx"
            + " WHERE child_webpage_uuid = :childUuid AND parent_webpage_uuid = :parentUuid;";
    dbi.withHandle(
        h -> {
          PreparedBatch batch = h.prepareBatch(query);
          int idx = 0;
          for (UUID uuidChild : children) {
            batch
                .bind("idx", idx++)
                .bind("childUuid", uuidChild)
                .bind("parentUuid", parentUuid)
                .add();
          }
          return batch.execute();
        });
    return true;
  }
}
