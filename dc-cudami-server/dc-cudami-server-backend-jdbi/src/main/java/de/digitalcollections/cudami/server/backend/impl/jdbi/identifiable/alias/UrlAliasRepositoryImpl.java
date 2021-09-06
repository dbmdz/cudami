package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.alias;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.StatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class UrlAliasRepositoryImpl extends JdbiRepositoryImpl implements UrlAliasRepository {

  public static final String TABLE_NAME = "url_aliases";
  public static final String TABLE_ALIAS = "ua";
  public static final String MAPPING_PREFIX = "ua";

  public static final String WEBSITESALIAS = "webs";

  private static final Map<String, String> PROPERTY_COLUMN_MAPPING;

  static {
    PROPERTY_COLUMN_MAPPING = new LinkedHashMap<>(10);
    PROPERTY_COLUMN_MAPPING.put("created", "created");
    PROPERTY_COLUMN_MAPPING.put("lastPublished", "last_published");
    PROPERTY_COLUMN_MAPPING.put("primary", "primary");
    PROPERTY_COLUMN_MAPPING.put("slug", "slug");
    PROPERTY_COLUMN_MAPPING.put("targetIdentifiableType", "target_identifiable_type");
    PROPERTY_COLUMN_MAPPING.put("targetEntityType", "target_entity_type");
    PROPERTY_COLUMN_MAPPING.put("targetLanguage", "target_language");
    PROPERTY_COLUMN_MAPPING.put("targetUuid", "target_uuid");
    PROPERTY_COLUMN_MAPPING.put("uuid", "uuid");
    PROPERTY_COLUMN_MAPPING.put("websiteUuid", "website_uuid");
  }

  public static String getSelectFields(boolean withWebsite) {
    // our own columns...
    Stream<String> selectFields =
        PROPERTY_COLUMN_MAPPING.entrySet().stream()
            .map(
                e ->
                    String.format(
                        "%s.%s %s_%s", TABLE_ALIAS, e.getValue(), MAPPING_PREFIX, e.getKey()));
    if (withWebsite) {
      // those of the website that we need too...
      selectFields =
          Stream.concat(
              selectFields,
              Stream.of("uuid", "label", "url")
                  .map(
                      col ->
                          String.format("%1$s.%2$s %3$s_%2$s", WEBSITESALIAS, col, WEBSITESALIAS)));
    }
    return selectFields.collect(Collectors.joining(", "));
  }

  public static final String WEBSITESJOIN =
      String.format(
          " LEFT JOIN websites %1$s ON %1$s.uuid = %2$s.website_uuid ", WEBSITESALIAS, TABLE_ALIAS);

  @Autowired
  public UrlAliasRepositoryImpl(Jdbi dbi) {
    super(dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX);
    dbi.registerRowMapper(BeanMapper.factory(UrlAlias.class, MAPPING_PREFIX));
  }

  @Override
  public int delete(List<UUID> urlAliasUuids) throws UrlAliasRepositoryException {
    if (urlAliasUuids.isEmpty()) {
      return 0;
    }
    String sql = "DELETE FROM " + this.tableName + " WHERE uuid in (<aliasUuids>);";
    try {
      return this.dbi.withHandle(
          h -> h.createUpdate(sql).bindList("aliasUuids", urlAliasUuids).execute());
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }

  private UUID extractWebsiteUuid(UrlAlias urlAlias) {
    if (urlAlias == null) {
      return null;
    }
    return urlAlias.getWebsite() != null ? urlAlias.getWebsite().getUuid() : null;
  }

  @Override
  public SearchPageResponse<LocalizedUrlAliases> find(SearchPageRequest searchPageRequest)
      throws UrlAliasRepositoryException {
    StringBuilder commonSql =
        new StringBuilder(" FROM " + this.tableName + " AS " + this.tableAlias + WEBSITESJOIN);
    Map<String, Object> bindings = new HashMap<>();

    this.addFiltering(searchPageRequest, commonSql);
    if (StringUtils.hasText(searchPageRequest.getQuery())) {
      commonSql
          .append(commonSql.toString().matches("(?i).+\\s+WHERE .*") ? " AND " : " WHERE ")
          .append(this.tableAlias + ".slug like '%' || :searchTerm || '%' ");
      bindings.put("searchTerm", searchPageRequest.getQuery());
    }

    long count;
    try {
      count =
          this.dbi.withHandle(
              h ->
                  h.createQuery("SELECT count(*) " + commonSql.toString())
                      .bindMap(bindings)
                      .mapTo(Long.class)
                      .findOne()
                      .orElse(0L));
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }

    if (searchPageRequest.getSorting() == null) {
      commonSql.append(" ORDER BY slug ");
    }
    this.addPageRequestParams(searchPageRequest, commonSql);

    try {
      UrlAlias[] resultset =
          this.dbi.withHandle(
              h ->
                  h.createQuery("SELECT " + getSelectFields(true) + commonSql.toString())
                      .bindMap(bindings)
                      .reduceRows(this::mapRowToUrlAlias)
                      .toArray(UrlAlias[]::new));
      return new SearchPageResponse<>(
          List.of(new LocalizedUrlAliases(resultset)), searchPageRequest, count);
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }

  @Override
  public LocalizedUrlAliases findAllForTarget(UUID uuid) throws UrlAliasRepositoryException {
    if (uuid == null) {
      return new LocalizedUrlAliases();
    }
    String sql =
        "SELECT "
            + getSelectFields(true)
            + " FROM "
            + this.tableName
            + " AS "
            + this.tableAlias
            + WEBSITESJOIN
            + " WHERE "
            + this.tableAlias
            + ".target_uuid = :uuid ORDER BY \"primary\", slug;";
    try {
      UrlAlias[] resultset =
          this.dbi.withHandle(
              h ->
                  h.createQuery(sql)
                      .bind("uuid", uuid)
                      .reduceRows(this::mapRowToUrlAlias)
                      .toArray(UrlAlias[]::new));
      return new LocalizedUrlAliases(resultset);
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }

  @Override
  public LocalizedUrlAliases findAllPrimaryLinks(String slug) throws UrlAliasRepositoryException {
    if (!StringUtils.hasText(slug)) {
      return new LocalizedUrlAliases();
    }
    return this.findMainLinks(false, null, slug);
  }

  protected LocalizedUrlAliases findMainLinks(boolean useWebsite, UUID websiteUuid, String slug)
      throws UrlAliasRepositoryException {
    StringBuilder innerSel =
        new StringBuilder("(SELECT target_uuid FROM " + this.tableName + " WHERE slug = :slug");
    if (useWebsite) {
      innerSel.append(" AND website_uuid ").append(websiteUuid != null ? "= :uuid" : "IS NULL");
    }
    innerSel.append(")");
    Map<String, Object> bindings = new HashMap<>();
    bindings.put("slug", slug);
    StringBuilder sql =
        new StringBuilder(
            "SELECT "
                + getSelectFields(true)
                + " FROM "
                + this.tableName
                + " AS "
                + this.tableAlias
                + WEBSITESJOIN
                + " WHERE "
                + this.tableAlias
                + ".primary = true AND "
                + this.tableAlias
                + ".target_uuid IN "
                + innerSel.toString());
    if (useWebsite) {
      sql.append(" AND " + this.tableAlias + ".website_uuid ")
          .append(websiteUuid != null ? "= :uuid" : "IS NULL");
      bindings.put("uuid", websiteUuid);
    }
    try {
      UrlAlias[] resultset =
          this.dbi.withHandle(
              h ->
                  h.createQuery(sql.toString())
                      .bindMap(bindings)
                      .reduceRows(this::mapRowToUrlAlias)
                      .toArray(UrlAlias[]::new));
      return new LocalizedUrlAliases(resultset);
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }

  @Override
  public LocalizedUrlAliases findPrimaryLinksForWebsite(UUID websiteUuid, String slug)
      throws UrlAliasRepositoryException {
    if (!StringUtils.hasText(slug)) {
      return new LocalizedUrlAliases();
    }
    return this.findMainLinks(true, websiteUuid, slug);
  }

  @Override
  public UrlAlias findOne(UUID uuid) throws UrlAliasRepositoryException {
    if (uuid == null) {
      return null;
    }
    String sql =
        "SELECT "
            + getSelectFields(true)
            + " FROM "
            + this.tableName
            + " AS "
            + this.tableAlias
            + WEBSITESJOIN
            + String.format(" WHERE %s.uuid = :uuid;", this.tableAlias);
    try {
      return this.dbi.withHandle(
          h ->
              h.createQuery(sql)
                  .bind("uuid", uuid)
                  .reduceRows(this::mapRowToUrlAlias)
                  .findFirst()
                  .orElse(null));
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return List.of("created", "lastPublished", "\"primary\"", "slug", "targetLanguage");
  }

  private String getAssignmentsForUpdate() {
    return PROPERTY_COLUMN_MAPPING.entrySet().stream()
        .filter(e -> !(e.getKey().equals("created") || e.getKey().equals("uuid")))
        .map(
            e ->
                String.format(
                    "%s = :%s",
                    e.getValue().equals("primary") ? "\"primary\"" : e.getValue(), e.getKey()))
        .collect(Collectors.joining(", "));
  }

  @Override
  protected String getColumnName(String modelProperty) {
    return this.tableAlias + "." + PROPERTY_COLUMN_MAPPING.get(modelProperty);
  }

  private String getColumnsForInsert() {
    return PROPERTY_COLUMN_MAPPING.values().stream()
        .map(col -> col.equals("primary") ? "\"primary\"" : col)
        .collect(Collectors.joining(", "));
  }

  private String getPlaceholdersForInsert() {
    return PROPERTY_COLUMN_MAPPING.keySet().stream()
        .map(prop -> ":" + prop)
        .collect(Collectors.joining(", "));
  }

  @Override
  public boolean hasUrlAlias(UUID websiteUuid, String slug) throws UrlAliasRepositoryException {
    if (!StringUtils.hasText(slug)) {
      throw new UrlAliasRepositoryException(
          "UrlAliasRepository.hasUrlAlias: Parameter 'slug' must not be null or empty.");
    }
    Map<String, Object> bindings = new HashMap<>();
    bindings.put("websiteUuid", websiteUuid);
    bindings.put("slug", slug);
    try {
      return 0
          < this.dbi.withHandle(
              h ->
                  h.createQuery(
                          "SELECT uuid FROM "
                              + this.tableName
                              + " WHERE website_uuid "
                              + (websiteUuid != null ? "= :websiteUuid " : "IS NULL ")
                              + "AND slug = :slug;")
                      .bindMap(bindings)
                      .reduceRows(0, (count, row) -> ++count));
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }

  private void mapRowToUrlAlias(Map<UUID, UrlAlias> map, RowView row) {
    UrlAlias alias =
        map.compute(
            row.getColumn(this.mappingPrefix + "_uuid", UUID.class),
            (uuid, urlAlias) -> urlAlias != null ? urlAlias : row.getRow(UrlAlias.class));
    if (alias != null && row.getColumn(WEBSITESALIAS + "_uuid", UUID.class) != null) {
      Website website = new Website(row.getColumn(WEBSITESALIAS + "_url", URL.class));
      website.setUuid(row.getColumn(WEBSITESALIAS + "_uuid", UUID.class));
      website.setLabel(row.getColumn(WEBSITESALIAS + "_label", LocalizedText.class));
      alias.setWebsite(website);
    }
  }

  @Override
  public UrlAlias save(UrlAlias urlAlias) throws UrlAliasRepositoryException {
    if (urlAlias == null) {
      return null;
    }
    if (urlAlias.getUuid() == null) {
      urlAlias.setUuid(UUID.randomUUID());
    }
    urlAlias.setCreated(LocalDateTime.now());
    String sql =
        "INSERT INTO "
            + this.tableName
            + " ("
            + this.getColumnsForInsert()
            + ") VALUES ("
            + this.getPlaceholdersForInsert()
            + ") RETURNING uuid;";
    try {
      UUID newUuid =
          this.dbi.withHandle(
              h ->
                  h.createQuery(sql)
                      .bindBean(urlAlias)
                      .bind("websiteUuid", this.extractWebsiteUuid(urlAlias))
                      .mapTo(UUID.class)
                      .findOne()
                      .orElse(null));
      return this.findOne(newUuid);
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }

  @Override
  public UrlAlias update(UrlAlias urlAlias) throws UrlAliasRepositoryException {
    if (urlAlias == null) {
      return null;
    }
    String sql =
        "UPDATE "
            + this.tableName
            + " SET "
            + this.getAssignmentsForUpdate()
            + " WHERE uuid = :uuid;";
    try {
      int affected =
          this.dbi.withHandle(
              h ->
                  h.createUpdate(sql)
                      .bindBean(urlAlias)
                      .bind("websiteUuid", this.extractWebsiteUuid(urlAlias))
                      .execute());
      if (affected != 1) {
        throw new UrlAliasRepositoryException(
            String.format(
                "Update of '%s' went wrong. Affected rows: %d", urlAlias.getUuid(), affected));
      }
      return this.findOne(urlAlias.getUuid());
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }
}
