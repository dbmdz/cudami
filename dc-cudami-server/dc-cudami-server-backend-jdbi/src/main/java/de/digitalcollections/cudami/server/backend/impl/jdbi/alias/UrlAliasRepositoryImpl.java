package de.digitalcollections.cudami.server.backend.impl.jdbi.alias;

import de.digitalcollections.cudami.server.backend.api.repository.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.JdbiRepositoryImpl;
import de.digitalcollections.model.alias.LocalizedUrlAliases;
import de.digitalcollections.model.alias.UrlAlias;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UrlAliasRepositoryImpl extends JdbiRepositoryImpl implements UrlAliasRepository {

  public static final String TABLE_NAME = "url_aliases";
  public static final String TABLE_ALIAS = "ua";
  public static final String MAPPING_PREFIX = "ua";

  private static final Map<String, String> PROPERTY_COLUMN_MAPPING;

  static {
    PROPERTY_COLUMN_MAPPING = new LinkedHashMap<>(9);
    PROPERTY_COLUMN_MAPPING.put("created", "created");
    PROPERTY_COLUMN_MAPPING.put("lastPublished", "last_published");
    PROPERTY_COLUMN_MAPPING.put("mainAlias", "is_main_alias");
    PROPERTY_COLUMN_MAPPING.put("slug", "slug");
    PROPERTY_COLUMN_MAPPING.put("targetLanguage", "target_language");
    PROPERTY_COLUMN_MAPPING.put("targetType", "target_type");
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
    return List.of("created", "lastPublished", "mainAlias", "slug", "targetLanguage");
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
  public int delete(List<UUID> urlAliasUuids) {
    if (urlAliasUuids.isEmpty()) {
      return 0;
    }
    String sql = "DELETE FROM " + TABLE_NAME + " WHERE uuid in (<aliasUuids>);";
    return this.dbi.withHandle(
        h -> h.createUpdate(sql).bindList("aliasUuids", urlAliasUuids).execute());
  }

  @Override
  public LocalizedUrlAliases findAllForTarget(UUID uuid) {
    if (uuid == null) {
      return new LocalizedUrlAliases();
    }
    String sql =
        "SELECT * FROM " + TABLE_NAME + " WHERE target_uuid = ? ORDER BY is_main_alias, slug;";
    List<UrlAlias> resultset =
        this.dbi.withHandle(h -> h.createQuery(sql).bind(0, uuid).mapTo(UrlAlias.class).list());
    return new LocalizedUrlAliases((UrlAlias[]) resultset.toArray());
  }

  @Override
  public UrlAlias findMainLink(UUID uuid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public UrlAlias findOne(UUID uuid) {
    if (uuid == null) {
      return null;
    }
    String sql = "select * from " + TABLE_NAME + " where uuid = ?;";
    return this.dbi.withHandle(
        h -> h.createQuery(sql).bind(0, uuid).mapToBean(UrlAlias.class).findFirst().orElse(null));
  }

  @Override
  public UrlAlias save(UrlAlias urlAlias) {
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
    return this.dbi.withHandle(
        h ->
            h.createQuery(sql).bindBean(urlAlias).mapToBean(UrlAlias.class).findOne().orElse(null));
  }

  @Override
  public UrlAlias update(UrlAlias urlAlias) {
    if (urlAlias == null) {
      return null;
    }
    String sql =
        "UPDATE "
            + TABLE_NAME
            + " SET "
            + this.getAssignmentsForUpdate()
            + " WHERE uuid = :uuid RETURNING *;";
    return this.dbi.withHandle(
        h -> h.createQuery(sql).bindBean(urlAlias).mapToBean(UrlAlias.class).findOne().get());
  }
}
