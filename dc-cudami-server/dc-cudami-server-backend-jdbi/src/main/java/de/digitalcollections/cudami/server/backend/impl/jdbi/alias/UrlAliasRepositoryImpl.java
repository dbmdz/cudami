package de.digitalcollections.cudami.server.backend.impl.jdbi.alias;

import de.digitalcollections.cudami.server.backend.api.repository.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.UrlAliasRepositoryException;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.identifiable.alias.LocalizedUrlAliases;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.statement.StatementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class UrlAliasRepositoryImpl extends JdbiRepositoryImpl implements UrlAliasRepository {

  public static final String TABLE_NAME = "url_aliases";
  public static final String TABLE_ALIAS = "ua";
  public static final String MAPPING_PREFIX = "ua";

  private static final Map<String, String> PROPERTY_COLUMN_MAPPING;

  static {
    PROPERTY_COLUMN_MAPPING = new LinkedHashMap<>(10);
    PROPERTY_COLUMN_MAPPING.put("created", "created");
    PROPERTY_COLUMN_MAPPING.put("lastPublished", "last_published");
    PROPERTY_COLUMN_MAPPING.put("primary", "\"primary\"");
    PROPERTY_COLUMN_MAPPING.put("slug", "slug");
    PROPERTY_COLUMN_MAPPING.put("targetIdentifiableType", "target_identifiable_type");
    PROPERTY_COLUMN_MAPPING.put("targetEntityType", "target_entity_type");
    PROPERTY_COLUMN_MAPPING.put("targetLanguage", "target_language");
    PROPERTY_COLUMN_MAPPING.put("targetUuid", "target_uuid");
    PROPERTY_COLUMN_MAPPING.put("uuid", "uuid");
    PROPERTY_COLUMN_MAPPING.put("websiteUuid", "website_uuid");
  }

  @Autowired
  public UrlAliasRepositoryImpl(Jdbi dbi) {
    super(dbi, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    return List.of("created", "lastPublished", "\"primary\"", "slug", "targetLanguage");
  }

  @Override
  protected String getColumnName(String modelProperty) {
    return TABLE_ALIAS + "." + PROPERTY_COLUMN_MAPPING.get(modelProperty);
  }

  private String getColumnsForInsert() {
    return String.join(", ", PROPERTY_COLUMN_MAPPING.values());
  }

  private String getPlaceholdersForInsert() {
    return PROPERTY_COLUMN_MAPPING.keySet().stream()
        .map(prop -> ":" + prop)
        .collect(Collectors.joining(", "));
  }

  private String getAssignmentsForUpdate() {
    return PROPERTY_COLUMN_MAPPING.entrySet().stream()
        .filter(e -> !(e.getKey().equals("created") || e.getKey().equals("uuid")))
        .map(e -> String.format("%s = :%s", e.getValue(), e.getKey()))
        .collect(Collectors.joining(", "));
  }

  @Override
  public int delete(List<UUID> urlAliasUuids) throws UrlAliasRepositoryException {
    if (urlAliasUuids.isEmpty()) {
      return 0;
    }
    String sql = "DELETE FROM " + TABLE_NAME + " WHERE uuid in (<aliasUuids>);";
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

  @Override
  public SearchPageResponse<LocalizedUrlAliases> find(SearchPageRequest searchPageRequest)
      throws UrlAliasRepositoryException {
    StringBuilder commonSql = new StringBuilder("FROM " + TABLE_NAME + " AS " + TABLE_ALIAS);
    Map<String, Object> bindings = new HashMap<>();

    this.addFiltering(searchPageRequest, commonSql, bindings);
    if (StringUtils.hasText(searchPageRequest.getQuery())) {
      commonSql
          .append(commonSql.toString().matches("(?i).+\\s+WHERE .*") ? " AND " : " WHERE ")
          .append(TABLE_ALIAS + ".slug like '%' || :searchTerm || '%' ");
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
                  h
                      .createQuery("SELECT * " + commonSql.toString())
                      .bindMap(bindings)
                      .mapToBean(UrlAlias.class)
                      .stream()
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
        "SELECT * FROM " + TABLE_NAME + " WHERE target_uuid = :uuid ORDER BY \"primary\", slug;";
    try {
      UrlAlias[] resultset =
          this.dbi.withHandle(
              h ->
                  h.createQuery(sql).bind("uuid", uuid).mapToBean(UrlAlias.class).stream()
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
  public LocalizedUrlAliases findMainLinks(UUID websiteUuid, String slug)
      throws UrlAliasRepositoryException {
    if (websiteUuid == null || !StringUtils.hasText(slug)) {
      return new LocalizedUrlAliases();
    }
    String innerSel =
        "(SELECT target_uuid FROM " + TABLE_NAME + " WHERE website_uuid = :uuid AND slug = :slug)";
    String sql =
        "SELECT * FROM "
            + TABLE_NAME
            + " WHERE website_uuid = :uuid AND \"primary\" = true AND target_uuid = "
            + innerSel;
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", websiteUuid);
    argumentMappings.put("slug", slug);
    try {
      UrlAlias[] resultset =
          this.dbi.withHandle(
              h ->
                  h.createQuery(sql).bindMap(argumentMappings).mapToBean(UrlAlias.class).stream()
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
  public UrlAlias findOne(UUID uuid) throws UrlAliasRepositoryException {
    if (uuid == null) {
      return null;
    }
    String sql = "select * from " + TABLE_NAME + " where uuid = :uuid;";
    try {
      return this.dbi.withHandle(
          h ->
              h.createQuery(sql)
                  .bind("uuid", uuid)
                  .mapToBean(UrlAlias.class)
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
            + TABLE_NAME
            + " ("
            + this.getColumnsForInsert()
            + ") VALUES ("
            + this.getPlaceholdersForInsert()
            + ") RETURNING *;";
    try {
      return this.dbi.withHandle(
          h ->
              h.createQuery(sql)
                  .bindBean(urlAlias)
                  .mapToBean(UrlAlias.class)
                  .findOne()
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
  public UrlAlias update(UrlAlias urlAlias) throws UrlAliasRepositoryException {
    if (urlAlias == null) {
      return null;
    }
    String sql =
        "UPDATE "
            + TABLE_NAME
            + " SET "
            + this.getAssignmentsForUpdate()
            + " WHERE uuid = :uuid RETURNING *;";
    try {
      return this.dbi.withHandle(
          h ->
              h.createQuery(sql)
                  .bindBean(urlAlias)
                  .mapToBean(UrlAlias.class)
                  .findOne()
                  .orElse(null));
    } catch (StatementException e) {
      String detailMessage = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
      throw new UrlAliasRepositoryException(
          String.format("The SQL statement is defective: %s", detailMessage), e);
    } catch (JdbiException e) {
      throw new UrlAliasRepositoryException(e);
    }
  }
}
