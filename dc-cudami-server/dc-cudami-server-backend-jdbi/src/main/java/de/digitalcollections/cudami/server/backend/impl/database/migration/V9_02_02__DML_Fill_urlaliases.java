package de.digitalcollections.cudami.server.backend.impl.database.migration;

import static de.digitalcollections.model.identifiable.entity.EntityType.AGENT;
import static de.digitalcollections.model.identifiable.entity.EntityType.ARTICLE;
import static de.digitalcollections.model.identifiable.entity.EntityType.AUDIO;
import static de.digitalcollections.model.identifiable.entity.EntityType.BOOK;
import static de.digitalcollections.model.identifiable.entity.EntityType.COLLECTION;
import static de.digitalcollections.model.identifiable.entity.EntityType.CORPORATE_BODY;
import static de.digitalcollections.model.identifiable.entity.EntityType.ENTITY;
import static de.digitalcollections.model.identifiable.entity.EntityType.EVENT;
import static de.digitalcollections.model.identifiable.entity.EntityType.EXPRESSION;
import static de.digitalcollections.model.identifiable.entity.EntityType.FAMILY;
import static de.digitalcollections.model.identifiable.entity.EntityType.GEOLOCATION;
import static de.digitalcollections.model.identifiable.entity.EntityType.HEADWORD_ENTRY;
import static de.digitalcollections.model.identifiable.entity.EntityType.IMAGE;
import static de.digitalcollections.model.identifiable.entity.EntityType.ITEM;
import static de.digitalcollections.model.identifiable.entity.EntityType.MANIFESTATION;
import static de.digitalcollections.model.identifiable.entity.EntityType.OBJECT_3D;
import static de.digitalcollections.model.identifiable.entity.EntityType.PERSON;
import static de.digitalcollections.model.identifiable.entity.EntityType.PLACE;
import static de.digitalcollections.model.identifiable.entity.EntityType.PROJECT;
import static de.digitalcollections.model.identifiable.entity.EntityType.TOPIC;
import static de.digitalcollections.model.identifiable.entity.EntityType.VIDEO;
import static de.digitalcollections.model.identifiable.entity.EntityType.WEBSITE;
import static de.digitalcollections.model.identifiable.entity.EntityType.WORK;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONObject;
import de.digitalcollections.commons.web.SlugGenerator;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Website;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
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
 *   <li>drop table url_aliases;
 *   <li>delete from flyway_schema_history where version='9.02.02';
 * </ul>
 */
@SuppressWarnings("checkstyle:typename")
public class V9_02_02__DML_Fill_urlaliases extends BaseJavaMigration {

  // Cannot be more elegant, since we want to allow null values
  private static final Map<EntityType, String> ENTITYMIGRATIONTABLES = new HashMap<>();

  static {
    ENTITYMIGRATIONTABLES.put(AGENT, null);
    ENTITYMIGRATIONTABLES.put(ARTICLE, "articles");
    ENTITYMIGRATIONTABLES.put(AUDIO, null);
    ENTITYMIGRATIONTABLES.put(BOOK, null);
    ENTITYMIGRATIONTABLES.put(COLLECTION, "collections");
    ENTITYMIGRATIONTABLES.put(CORPORATE_BODY, "corporatebodies");
    // FIXME: This must be removed dynamically by the configuration
    // ENTITYMIGRATIONTABLES.put(DIGITAL_OBJECT, "digitalobjects");
    ENTITYMIGRATIONTABLES.put(ENTITY, null);
    ENTITYMIGRATIONTABLES.put(EVENT, null);
    ENTITYMIGRATIONTABLES.put(EXPRESSION, null);
    ENTITYMIGRATIONTABLES.put(FAMILY, "familynames");
    ENTITYMIGRATIONTABLES.put(GEOLOCATION, "geolocations");
    ENTITYMIGRATIONTABLES.put(HEADWORD_ENTRY, null);
    ENTITYMIGRATIONTABLES.put(IMAGE, null);
    ENTITYMIGRATIONTABLES.put(ITEM, "items");
    ENTITYMIGRATIONTABLES.put(MANIFESTATION, null);
    ENTITYMIGRATIONTABLES.put(OBJECT_3D, null);
    ENTITYMIGRATIONTABLES.put(PERSON, "persons");
    ENTITYMIGRATIONTABLES.put(PLACE, null);
    ENTITYMIGRATIONTABLES.put(PROJECT, "projects");
    ENTITYMIGRATIONTABLES.put(TOPIC, "topics");
    ENTITYMIGRATIONTABLES.put(VIDEO, null);
    ENTITYMIGRATIONTABLES.put(WEBSITE, "websites");
    ENTITYMIGRATIONTABLES.put(WORK, "works");
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(V9_02_02__DML_Fill_urlaliases.class);

  private final SlugGenerator slugGenerator = new SlugGenerator();

  @Override
  public void migrate(Context context) throws Exception {

    // TODO Retrieve configuration and filter ENTITIYMIGRATIONTABLES accordingly!  (and remove
    // comment accordingly)

    final SingleConnectionDataSource connectionDataSource =
        new SingleConnectionDataSource(context.getConnection(), true);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(connectionDataSource.getConnection());

    // Webpages have to be migrated individually, since their URLAliases depend on the
    // the websites, they are bound, too
    migrateWebpages(jdbcTemplate);

    // All other entities have only generic URLAliases
    ENTITYMIGRATIONTABLES.forEach(
        (entityType, tableName) -> {
          if (tableName != null) {
            try {
              migrateIdentifiables(jdbcTemplate, tableName, entityType);
            } catch (SQLException e) {
              throw new RuntimeException("Cannot migrate " + entityType + ": " + e, e);
            }
          }
        });

    // FIXME Das ist nur zu Demonstrationszwecken!
    throw new RuntimeException("Force trigger rollback");
  }

  private void migrateWebpages(JdbcTemplate jdbcTemplate) throws SQLException {
    String selectQuery =
        "SELECT w.uuid AS w_uuid, w.label AS label, ww.website_uuid AS ws_uuid FROM webpages w, website_webpages ww WHERE ww.webpage_uuid=w.uuid ORDER BY ws_uuid,w_uuid";
    List<Map<String, String>> webpages = jdbcTemplate.queryForList(selectQuery);
    webpages.forEach(
        w -> {
          JSONObject jsonObject = new JSONObject(w.toString());
          UUID uuid = UUID.fromString(jsonObject.getString("w_uuid"));
          UUID websiteUuid = UUID.fromString(jsonObject.getString("ws_uuid"));
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
          } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot parse " + w + ": " + e, e);
          }
        });
    LOGGER.info("Successfully added {} UrlAliases for webpages", webpages.size());
  }

  private void migrateIdentifiables(
      JdbcTemplate jdbcTemplate, String tableName, EntityType entityType) throws SQLException {
    String selectQuery = String.format("SELECT uuid,label FROM %s ORDER by uuid", tableName);
    List<Map<String, String>> identifiables = jdbcTemplate.queryForList(selectQuery);

    if (identifiables.isEmpty()) {
      LOGGER.info("No UrlAliases to add for {}", tableName);
      return;
    }

    identifiables.forEach(
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
    LOGGER.info("Successfully added {} UrlAliases for {}", identifiables.size(), tableName);
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
      LOGGER.warn("Building slug with suffix: " + slug);
    }

    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setUuid(UUID.randomUUID());
    urlAlias.setTargetLanguage(locale);
    urlAlias.setPrimary(true);
    urlAlias.setTargetUuid(uuid);
    urlAlias.setTargetIdentifiableType(identifiableType);
    urlAlias.setTargetEntityType(entityType);
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
        "insert into url_aliases (uuid,created,last_published,\"primary\",slug,target_entity_type,target_identifiable_type,target_language,target_uuid,website_uuid) VALUES(?::uuid,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,?,?,?,?,?::uuid,?::uuid);";
    jdbcTemplate.update(
        updateQuery,
        urlAlias.getUuid().toString(),
        urlAlias.isPrimary(),
        urlAlias.getSlug(),
        urlAlias.getTargetEntityType() != null ? urlAlias.getTargetEntityType().toString() : null,
        urlAlias.getTargetIdentifiableType().toString(),
        urlAlias.getTargetLanguage().toString(),
        urlAlias.getTargetUuid().toString(),
        urlAlias.getWebsite() != null ? urlAlias.getWebsite().getUuid().toString() : null);
  }

  private boolean hasUrlAlias(
      JdbcTemplate jdbcTemplate, String slug, UUID uuid, Locale locale, UUID websiteUuid)
      throws SQLException {

    if (websiteUuid == null) {
      return jdbcTemplate.queryForInt(
              "SELECT count(*) from url_aliases where slug=? and target_uuid=uuid(?) and target_language=?",
              slug,
              uuid.toString(),
              locale.getLanguage())
          > 0;
    }

    return jdbcTemplate.queryForInt(
            "SELECT count(*) from url_aliases where slug=? and target_uuid=uuid(?) and target_language=? and website_uuid=uuid(?)",
            slug,
            uuid.toString(),
            locale.getLanguage(),
            websiteUuid.toString())
        > 0;
  }
}
