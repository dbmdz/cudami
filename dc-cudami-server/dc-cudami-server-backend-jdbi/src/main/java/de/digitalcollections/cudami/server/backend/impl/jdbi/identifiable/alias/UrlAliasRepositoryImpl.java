package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.alias;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
                  .map(col -> String.format("%1$s.%2$s %1$s_%2$s", WEBSITESALIAS, col)));
    }
    return selectFields.collect(Collectors.joining(", "));
  }

  public static final String WEBSITESJOIN =
      String.format(
          " LEFT JOIN websites %1$s ON %1$s.uuid = %2$s.website_uuid ", WEBSITESALIAS, TABLE_ALIAS);

  @Autowired
  public UrlAliasRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    super(
        dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, cudamiConfig.getOffsetForAlternativePaging());
    dbi.registerRowMapper(BeanMapper.factory(UrlAlias.class, MAPPING_PREFIX));
  }

  @Override
  public int delete(List<UUID> urlAliasUuids) throws UrlAliasRepositoryException {
    if (urlAliasUuids.isEmpty()) {
      return 0;
    }
    String sql = "DELETE FROM " + tableName + " WHERE uuid in (<aliasUuids>);";
    try {
      return dbi.withHandle(
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
        new StringBuilder(" FROM " + tableName + " AS " + tableAlias + WEBSITESJOIN);

    Filtering filtering = searchPageRequest.getFiltering();
    FilterCriterion slug =
        StringUtils.hasText(searchPageRequest.getQuery())
            ? FilterCriterion.builder()
                .withExpression("slug")
                .contains(searchPageRequest.getQuery())
                .build()
            : null;
    if (filtering == null) {
      filtering = Filtering.builder().add(slug).build();
    } else {
      filtering.add(slug);
    }
    Map<String, Object> bindings = new HashMap<>();
    addFiltering(filtering, commonSql, bindings);

    long count;
    try {
      count =
          dbi.withHandle(
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

    if (!searchPageRequest.hasSorting()) {
      searchPageRequest.setSorting(new Sorting("slug"));
    }
    commonSql.insert(0, String.format("SELECT %s ", getSelectFields(true)));
    addPageRequestParams(searchPageRequest, commonSql);

    try {
      UrlAlias[] resultset =
          dbi.withHandle(
              h ->
                  h.createQuery(commonSql.toString())
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
  public LocalizedUrlAliases getAllForTarget(UUID uuid) throws UrlAliasRepositoryException {
    if (uuid == null) {
      return new LocalizedUrlAliases();
    }
    StringBuilder sql =
        new StringBuilder(
            "SELECT "
                + getSelectFields(true)
                + " FROM "
                + tableName
                + " AS "
                + tableAlias
                + WEBSITESJOIN);
    Map<String, Object> bindings = new HashMap<>();
    Filtering target =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("targetUuid").isEquals(uuid).build())
            .build();
    addFiltering(target, sql, bindings);
    try {
      UrlAlias[] resultset =
          dbi.withHandle(
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
  public LocalizedUrlAliases findAllPrimaryLinks(String slug) throws UrlAliasRepositoryException {
    if (!StringUtils.hasText(slug)) {
      return new LocalizedUrlAliases();
    }
    return findMainLinks(false, null, slug, false);
  }

  private LocalizedUrlAliases findMainLinks(
      boolean useWebsite, UUID websiteUuid, String slug, boolean considerLanguage)
      throws UrlAliasRepositoryException {
    StringBuilder innerSel =
        new StringBuilder(
            String.format(
                "(SELECT %2$s.target_uuid, %2$s.target_language FROM %1$s AS %2$s ",
                tableName, tableAlias));
    Filtering innerFiltering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("slug").isEquals(slug).build())
            .build();
    if (useWebsite) {
      innerFiltering.add(
          FilterCriterion.builder().withExpression("websiteUuid").isEquals(websiteUuid).build());
    }
    Map<String, Object> bindings = new HashMap<>();
    addFiltering(innerFiltering, innerSel, bindings);
    innerSel.append(")");
    StringBuilder sql =
        new StringBuilder(
            "WITH target (uuid, language) AS "
                + innerSel.toString()
                + " SELECT "
                + getSelectFields(true)
                + " FROM "
                + tableName
                + " AS "
                + tableAlias
                + WEBSITESJOIN
                + " WHERE "
                + tableAlias
                + ".target_uuid IN (SELECT uuid FROM target)");
    if (considerLanguage) {
      sql.append(" AND " + tableAlias + ".target_language IN (SELECT language FROM target)");
    }
    Filtering outerFiltering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("primary").isEquals(true).build())
            .build();
    if (useWebsite) {
      outerFiltering.add(
          FilterCriterion.builder().withExpression("websiteUuid").isEquals(websiteUuid).build());
    }
    addFiltering(outerFiltering, sql, bindings);
    try {
      UrlAlias[] resultset =
          dbi.withHandle(
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
  public LocalizedUrlAliases findPrimaryLinksForWebsite(
      UUID websiteUuid, String slug, boolean considerLanguage) throws UrlAliasRepositoryException {
    if (!StringUtils.hasText(slug)) {
      return new LocalizedUrlAliases();
    }
    return findMainLinks(true, websiteUuid, slug, considerLanguage);
  }

  @Override
  public UrlAlias getByUuid(UUID uuid) throws UrlAliasRepositoryException {
    if (uuid == null) {
      return null;
    }
    StringBuilder sql =
        new StringBuilder(
            "SELECT "
                + getSelectFields(true)
                + " FROM "
                + tableName
                + " AS "
                + tableAlias
                + WEBSITESJOIN);
    Map<String, Object> bindings = new HashMap<>();
    addFiltering(
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("uuid").isEquals(uuid).build())
            .build(),
        sql,
        bindings);
    try {
      return dbi.withHandle(
          h ->
              h.createQuery(sql.toString())
                  .bindMap(bindings)
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
    return new ArrayList<>(
        Arrays.asList("created", "lastPublished", "\"primary\"", "slug", "targetLanguage"));
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
    return tableAlias + "." + PROPERTY_COLUMN_MAPPING.get(modelProperty);
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
  protected String getUniqueField() {
    return "uuid";
  }

  @Override
  public boolean hasUrlAlias(String slug, UUID websiteUuid, Locale targetLanguage)
      throws UrlAliasRepositoryException {
    if (!StringUtils.hasText(slug)) {
      throw new UrlAliasRepositoryException(
          "UrlAliasRepository.hasUrlAlias: Parameter 'slug' must not be null or empty.");
    }
    StringBuilder sql = new StringBuilder("SELECT uuid FROM " + tableName + " AS " + tableAlias);
    Filtering filtering =
        Filtering.builder()
            .add(
                FilterCriterion.builder()
                    .withExpression("websiteUuid")
                    .isEquals(websiteUuid)
                    .build())
            .build();
    filtering.add(
        FilterCriterion.builder()
            .withExpression("targetLanguage")
            .isEquals(targetLanguage.getLanguage())
            .build());
    filtering.add(FilterCriterion.builder().withExpression("slug").isEquals(slug).build());
    Map<String, Object> bindings = new HashMap<>();
    addFiltering(filtering, sql, bindings);
    try {
      return 0
          < dbi.withHandle(
              h ->
                  h.createQuery(sql.toString())
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
            row.getColumn(mappingPrefix + "_uuid", UUID.class),
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
    if (urlAlias.getCreated() == null) {
      urlAlias.setCreated(LocalDateTime.now());
    }
    String sql =
        "INSERT INTO "
            + tableName
            + " ("
            + getColumnsForInsert()
            + ") VALUES ("
            + getPlaceholdersForInsert()
            + ") RETURNING uuid;";
    try {
      UUID newUuid =
          dbi.withHandle(
              h ->
                  h.createQuery(sql)
                      .bindBean(urlAlias)
                      .bind("websiteUuid", extractWebsiteUuid(urlAlias))
                      .bind("targetLanguage", urlAlias.getTargetLanguage().getLanguage())
                      .mapTo(UUID.class)
                      .findOne()
                      .orElse(null));
      return getByUuid(newUuid);
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
        "UPDATE " + tableName + " SET " + getAssignmentsForUpdate() + " WHERE uuid = :uuid;";
    try {
      int affected =
          dbi.withHandle(
              h ->
                  h.createUpdate(sql)
                      .bindBean(urlAlias)
                      .bind("websiteUuid", extractWebsiteUuid(urlAlias))
                      .bind("targetLanguage", urlAlias.getTargetLanguage().getLanguage())
                      .execute());
      if (affected != 1) {
        throw new UrlAliasRepositoryException(
            String.format(
                "Update of '%s' went wrong. Affected rows: %d", urlAlias.getUuid(), affected));
      }
      return getByUuid(urlAlias.getUuid());
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }
}
