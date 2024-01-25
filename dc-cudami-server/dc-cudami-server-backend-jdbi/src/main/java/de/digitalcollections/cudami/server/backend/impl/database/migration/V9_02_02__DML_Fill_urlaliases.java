package de.digitalcollections.cudami.server.backend.impl.database.migration;

import static de.digitalcollections.model.identifiable.entity.EntityType.ARTICLE;
import static de.digitalcollections.model.identifiable.entity.EntityType.COLLECTION;
import static de.digitalcollections.model.identifiable.entity.EntityType.CORPORATE_BODY;
import static de.digitalcollections.model.identifiable.entity.EntityType.DIGITAL_OBJECT;
import static de.digitalcollections.model.identifiable.entity.EntityType.GEOLOCATION;
import static de.digitalcollections.model.identifiable.entity.EntityType.ITEM;
import static de.digitalcollections.model.identifiable.entity.EntityType.PERSON;
import static de.digitalcollections.model.identifiable.entity.EntityType.PROJECT;
import static de.digitalcollections.model.identifiable.entity.EntityType.TOPIC;
import static de.digitalcollections.model.identifiable.entity.EntityType.WEBSITE;
import static de.digitalcollections.model.identifiable.entity.EntityType.WORK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONObject;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.config.SpringUtility;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.util.SlugGenerator;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Rollback:
 *
 * <ul>
 *   <li>delete from url_aliases;
 *   <li>delete from flyway_schema_history where version='9.02.02';
 * </ul>
 */
@SuppressWarnings("checkstyle:typename")
public class V9_02_02__DML_Fill_urlaliases extends BaseJavaMigration {
  // Cannot be more elegant, since we want to allow null values
  private static final Map<String, Pair<String, EntityType>> ENTITY_MIGRATION_TABLES =
      new LinkedHashMap<>();

  // for all the commented lines there doesn't exist a corresponding model class
  static {
    ENTITY_MIGRATION_TABLES.put(IdentifiableObjectType.AGENT.toString(), null);
    ENTITY_MIGRATION_TABLES.put(
        IdentifiableObjectType.ARTICLE.toString(), Pair.of("articles", ARTICLE));
    // ENTITY_MIGRATION_TABLES.put(AUDIO, null);
    // ENTITY_MIGRATION_TABLES.put(BOOK, null);
    ENTITY_MIGRATION_TABLES.put(
        IdentifiableObjectType.COLLECTION.toString(), Pair.of("collections", COLLECTION));
    ENTITY_MIGRATION_TABLES.put(
        IdentifiableObjectType.CORPORATE_BODY.toString(),
        Pair.of("corporatebodies", CORPORATE_BODY));
    ENTITY_MIGRATION_TABLES.put(
        IdentifiableObjectType.DIGITAL_OBJECT.toString(),
        Pair.of("digitalobjects", DIGITAL_OBJECT));
    ENTITY_MIGRATION_TABLES.put(IdentifiableObjectType.ENTITY.toString(), null);
    // ENTITY_MIGRATION_TABLES.put(EVENT, null);
    // ENTITY_MIGRATION_TABLES.put(EXPRESSION, null);
    ENTITY_MIGRATION_TABLES.put(IdentifiableObjectType.FAMILY.toString(), null);
    ENTITY_MIGRATION_TABLES.put(
        IdentifiableObjectType.GEO_LOCATION.toString(), Pair.of("geolocations", GEOLOCATION));
    ENTITY_MIGRATION_TABLES.put(IdentifiableObjectType.HEADWORD_ENTRY.toString(), null);
    // ENTITY_MIGRATION_TABLES.put(IMAGE, null);
    ENTITY_MIGRATION_TABLES.put(IdentifiableObjectType.ITEM.toString(), Pair.of("items", ITEM));
    ENTITY_MIGRATION_TABLES.put(IdentifiableObjectType.MANIFESTATION.toString(), null);
    // ENTITY_MIGRATION_TABLES.put(OBJECT_3D, null);
    ENTITY_MIGRATION_TABLES.put(
        IdentifiableObjectType.PERSON.toString(), Pair.of("persons", PERSON));
    // ENTITY_MIGRATION_TABLES.put(PLACE, null);
    ENTITY_MIGRATION_TABLES.put(
        IdentifiableObjectType.PROJECT.toString(), Pair.of("projects", PROJECT));
    ENTITY_MIGRATION_TABLES.put(IdentifiableObjectType.TOPIC.toString(), Pair.of("topics", TOPIC));
    // ENTITY_MIGRATION_TABLES.put(VIDEO, null);
    ENTITY_MIGRATION_TABLES.put(
        IdentifiableObjectType.WEBSITE.toString(), Pair.of("websites", WEBSITE));
    ENTITY_MIGRATION_TABLES.put(IdentifiableObjectType.WORK.toString(), Pair.of("works", WORK));
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(V9_02_02__DML_Fill_urlaliases.class);

  private final SlugGenerator slugGenerator = new SlugGenerator();

  private CudamiConfig cudamiConfig = SpringUtility.getBean(CudamiConfig.class);

  @Override
  public void migrate(Context context) throws Exception {
    final SingleConnectionDataSource connectionDataSource =
        new SingleConnectionDataSource(context.getConnection(), true);
    slugGenerator.setMaxLength(cudamiConfig.getUrlAlias().getMaxLength());

    JdbcTemplate jdbcTemplate = new JdbcTemplate(connectionDataSource.getConnection());

    if (jdbcTemplate.queryForInt("SELECT count(*) from url_aliases") > 0) {
      LOGGER.info("UrlAliases already existing, so nothing to do...");
      return;
    }

    // Webpages have to be migrated individually, since their URLAliases depend on the
    // the websites, they are bound, too
    migrateWebpages(jdbcTemplate);

    removeEntitiesNotToMigrate();

    // All other entities have only generic URLAliases
    ENTITY_MIGRATION_TABLES.forEach(
        (className, attrs) -> {
          if (attrs != null) {
            try {
              migrateEntities(jdbcTemplate, attrs.getLeft(), attrs.getRight());
            } catch (SQLException e) {
              throw new RuntimeException("Cannot migrate " + attrs.getRight() + ": " + e, e);
            }
          }
        });
  }

  private void removeEntitiesNotToMigrate() {
    if (cudamiConfig.getUrlAlias() != null
        && cudamiConfig.getUrlAlias().getGenerationExcludes() != null) {
      List<String> excludedEntities = cudamiConfig.getUrlAlias().getGenerationExcludes();
      LOGGER.info("Excluding UrlAlias generation for " + excludedEntities);
      excludedEntities.forEach(ENTITY_MIGRATION_TABLES::remove);
    }

    LOGGER.info(
        "To migrate="
            + ENTITY_MIGRATION_TABLES.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList()));
  }

  private void migrateWebpages(JdbcTemplate jdbcTemplate) throws SQLException {
    String selectQuery =
        "SELECT w.uuid AS w_uuid, w.label AS label, ww.website_uuid AS ws_uuid FROM webpages w, website_webpages ww WHERE ww.webpage_uuid=w.uuid ORDER BY ws_uuid,w_uuid";
    List<Map<String, String>> webpages = jdbcTemplate.queryForList(selectQuery);

    if (webpages.isEmpty()) {
      LOGGER.info("No UrlAliases to add for webpages");
      return;
    }

    LOGGER.info("Migrating webpages");

    webpages.forEach(
        w -> {
          JSONObject jsonObject = new JSONObject(w.toString());
          UUID uuid = UUID.fromString(jsonObject.getString("w_uuid"));
          UUID websiteUuid = UUID.fromString(jsonObject.getString("ws_uuid"));
          createUrlAliasAndMigrateSubpages(jdbcTemplate, jsonObject, uuid, websiteUuid);
        });
    LOGGER.info("Successfully added {} UrlAliases for webpages", webpages.size());
  }

  private void migrateSubpages(JdbcTemplate jdbcTemplate, UUID websiteUuid, UUID parentWebpageUuid)
      throws SQLException {
    String selectQuery =
        "SELECT w.uuid AS w_uuid, w.label AS label FROM webpages w INNER JOIN webpage_webpages ww ON ww.child_webpage_uuid=w.uuid WHERE ww.parent_webpage_uuid = '"
            + parentWebpageUuid
            + "'";
    List<Map<String, String>> webpages = jdbcTemplate.queryForList(selectQuery);

    if (webpages.isEmpty()) {
      return;
    }

    webpages.forEach(
        w -> {
          JSONObject jsonObject = new JSONObject(w.toString());
          UUID uuid = UUID.fromString(jsonObject.getString("w_uuid"));
          createUrlAliasAndMigrateSubpages(jdbcTemplate, jsonObject, uuid, websiteUuid);
        });
  }

  private void createUrlAliasAndMigrateSubpages(
      JdbcTemplate jdbcTemplate, JSONObject jsonObject, UUID uuid, UUID websiteUuid) {
    try {
      Map<String, String> labels =
          new ObjectMapper().readValue(jsonObject.getString("label"), HashMap.class);
      labels.forEach(
          (language, label) -> {
            UrlAlias urlAlias =
                buildUrlAlias(
                    jdbcTemplate,
                    null,
                    IdentifiableType.RESOURCE,
                    uuid,
                    language,
                    label,
                    websiteUuid);
            try {
              saveUrlAlias(jdbcTemplate, urlAlias);
            } catch (SQLException e) {
              throw new RuntimeException("Cannot save urlAlias " + urlAlias + ": " + e, e);
            }
          });
      try {
        migrateSubpages(jdbcTemplate, websiteUuid, uuid);
      } catch (SQLException e) {
        throw new RuntimeException(
            "Cannot migrate subpages for websiteUuid=" + websiteUuid + ", uuid=" + uuid + ": " + e,
            e);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Cannot parse " + jsonObject + ": " + e, e);
    }
  }

  private void migrateEntities(JdbcTemplate jdbcTemplate, String tableName, EntityType entityType)
      throws SQLException {
    String selectQuery = String.format("SELECT uuid,label FROM %s", tableName);
    List<Map<String, String>> entities = jdbcTemplate.queryForList(selectQuery);

    if (entities.isEmpty()) {
      LOGGER.info("No UrlAliases to add for {}", tableName);
      return;
    }

    LOGGER.info("Migrating {}", tableName);

    entities.forEach(
        w -> {
          JSONObject jsonObject = new JSONObject(w.toString());
          UUID uuid = UUID.fromString(jsonObject.getString("uuid"));

          try {
            Map<String, String> labels =
                new ObjectMapper().readValue(jsonObject.getString("label"), HashMap.class);
            labels.forEach(
                (language, label) -> {
                  UrlAlias urlAlias =
                      buildUrlAlias(
                          jdbcTemplate,
                          entityType,
                          IdentifiableType.ENTITY,
                          uuid,
                          language,
                          label,
                          null);
                  try {
                    saveUrlAlias(jdbcTemplate, urlAlias);
                  } catch (SQLException e) {
                    throw new RuntimeException("Cannot save urlAlias " + urlAlias + ": " + e, e);
                  }
                });
          } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot parse " + w + ": " + e, e);
          }
        });
    LOGGER.info("Successfully added {} UrlAliases for {}", entities.size(), tableName);
  }

  private UrlAlias buildUrlAlias(
      JdbcTemplate jdbcTemplate,
      EntityType entityType,
      IdentifiableType identifiableType,
      UUID uuid,
      String language,
      String label,
      UUID websiteUuid) {
    Locale locale = Locale.forLanguageTag(language);
    String baseSlug = slugGenerator.generateSlug(label);
    String slug = baseSlug;
    int suffix = 0;
    while (true) {
      try {
        if (!hasUrlAlias(jdbcTemplate, slug, uuid, locale, websiteUuid)) break;
      } catch (SQLException e) {
        throw new RuntimeException(
            "Cannot check availability of URLAlias for uuid="
                + uuid
                + ", locale="
                + locale
                + ", slug="
                + slug
                + ": "
                + e,
            e);
      }
      suffix++;
      slug = String.format("%s-%d", baseSlug, suffix);
    }

    if (!slug.equals(baseSlug)) {
      LOGGER.warn(
          "{}: Building slug with suffix={}", entityType != null ? entityType : "WEBPAGE", slug);
    }

    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setUuid(UUID.randomUUID());
    urlAlias.setTargetLanguage(locale);
    urlAlias.setPrimary(true);

    Identifiable target = Identifiable.builder().uuid(uuid).type(identifiableType).build();

    urlAlias.setTarget(target);
    urlAlias.setSlug(slug);
    if (websiteUuid != null) {
      Website website = new Website();
      website.setUuid(websiteUuid);
      urlAlias.setWebsite(website);
    }

    return urlAlias;
  }

  private void saveUrlAlias(JdbcTemplate jdbcTemplate, UrlAlias urlAlias) throws SQLException {
    String updateQuery =
        "INSERT INTO url_aliases (uuid,created,last_published,\"primary\",slug,target_identifiable_type,target_language,target_uuid,website_uuid) VALUES(?::uuid,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,?,?,?,?::uuid,?::uuid);";
    try {
      jdbcTemplate.update(
          updateQuery,
          urlAlias.getUuid().toString(),
          urlAlias.isPrimary(),
          urlAlias.getSlug(),
          urlAlias.getTarget().getType().toString(),
          urlAlias.getTargetLanguage().toString(),
          urlAlias.getTarget().getUuid().toString(),
          urlAlias.getWebsite() != null ? urlAlias.getWebsite().getUuid().toString() : null);
    } catch (SQLException e) {
      throw new SQLException("Cannot insert " + urlAlias + ":" + e, e);
    }
  }

  private boolean hasUrlAlias(
      JdbcTemplate jdbcTemplate, String slug, UUID uuid, Locale locale, UUID websiteUuid)
      throws SQLException {

    if (websiteUuid == null) {
      return jdbcTemplate.queryForInt(
              "SELECT count(*) FROM url_aliases WHERE website_uuid IS NULL AND slug=? AND target_language=?",
              slug,
              locale.getLanguage())
          > 0;
    }

    return jdbcTemplate.queryForInt(
            "SELECT count(*) FROM url_aliases WHERE slug=? AND target_language=? AND website_uuid=uuid(?)",
            slug,
            locale.getLanguage(),
            websiteUuid.toString())
        > 0;
  }
}
