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

@SuppressWarnings("checkstyle:typename")
public class V9_02_02__DML_Fill_urlaliases extends BaseJavaMigration {

  // Cannot be more elegant, since we want to allow null values
  private static final Map<EntityType, String> ENTITYMIGRATIONTABLES =
      new HashMap<EntityType, String>();

  static {
    ENTITYMIGRATIONTABLES.put(AGENT, null);
    ENTITYMIGRATIONTABLES.put(ARTICLE, "articles");
    ENTITYMIGRATIONTABLES.put(AUDIO, null);
    ENTITYMIGRATIONTABLES.put(BOOK, null);
    ENTITYMIGRATIONTABLES.put(COLLECTION, "collections");
    ENTITYMIGRATIONTABLES.put(CORPORATE_BODY, "corporatebodies");
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

  private SlugGenerator slugGenerator = new SlugGenerator();

  @Override
  public void migrate(Context context) throws Exception {

    final SingleConnectionDataSource connectionDataSource =
        new SingleConnectionDataSource(context.getConnection(), true);
    JdbcTemplate jdbcTemplate = new JdbcTemplate(connectionDataSource.getConnection());

    migrateIdentifiables(jdbcTemplate, "webpages", null, IdentifiableType.RESOURCE);

    ENTITYMIGRATIONTABLES.forEach(
        (entityType, tableName) -> {
          if (tableName != null) {
            try {
              migrateIdentifiables(jdbcTemplate, tableName, entityType, IdentifiableType.ENTITY);
            } catch (SQLException e) {
              throw new RuntimeException("Cannot migrate " + entityType + ": " + e, e);
            }
          }
        });

    // FIXME Das ist nur zu Demonstrationszwecken!
    throw new RuntimeException("Force trigger rollback");
  }

  private void migrateIdentifiables(
      JdbcTemplate jdbcTemplate,
      String tableName,
      EntityType entityType,
      IdentifiableType identifiableType)
      throws SQLException {
    String selectQuery = String.format("SELECT uuid,label FROM %s", tableName);
    List<Map<String, String>> identifiables = jdbcTemplate.queryForList(selectQuery);
    identifiables.forEach(
        w -> {
          JSONObject jsonObject = new JSONObject(w.toString());
          UUID uuid = UUID.fromString(jsonObject.getString("uuid"));

          try {
            HashMap labels =
                new ObjectMapper().readValue(jsonObject.getString("label"), HashMap.class);
            labels.forEach(
                (language, label) -> {
                  UrlAlias urlAlias =
                      buildUrlAlias(
                          jdbcTemplate,
                          entityType,
                          identifiableType,
                          uuid,
                          (String) language,
                          (String) label);
                  try {
                    saveCommonUrlAlias(jdbcTemplate, urlAlias);
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
      String label) {
    Locale locale = Locale.forLanguageTag(language);
    String baseSlug = slugGenerator.generateSlug(label);
    String slug = baseSlug;
    int suffix = 0;
    while (true) {
      try {
        if (!hasUrlAlias(jdbcTemplate, slug, uuid, locale)) break;
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

    UrlAlias urlAlias = new UrlAlias();
    urlAlias.setUuid(UUID.randomUUID());
    urlAlias.setTargetLanguage(locale);
    urlAlias.setPrimary(true);
    urlAlias.setTargetUuid(uuid);
    urlAlias.setTargetIdentifiableType(identifiableType);
    urlAlias.setTargetEntityType(entityType);
    urlAlias.setSlug(slug);
    return urlAlias;
  }

  private void saveCommonUrlAlias(JdbcTemplate jdbcTemplate, UrlAlias urlAlias)
      throws SQLException {
    String updateQuery =
        "insert into url_aliases (uuid,created,last_published,\"primary\",slug,target_entity_type,target_identifiable_type,target_language,target_uuid) VALUES(?::uuid,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,?,?,?,?,?::uuid);";
    jdbcTemplate.update(
        updateQuery,
        urlAlias.getUuid().toString(),
        urlAlias.isPrimary(),
        urlAlias.getSlug(),
        urlAlias.getTargetEntityType() != null ? urlAlias.getTargetEntityType().toString() : null,
        urlAlias.getTargetIdentifiableType().toString(),
        urlAlias.getTargetLanguage().toString(),
        urlAlias.getTargetUuid().toString());
  }

  private boolean hasUrlAlias(JdbcTemplate jdbcTemplate, String slug, UUID uuid, Locale locale)
      throws SQLException {
    return jdbcTemplate.queryForInt(
            "SELECT count(*) from url_aliases where slug=? and target_uuid=uuid(?) and target_language=?",
            slug,
            uuid.toString(),
            locale.getLanguage())
        > 0;
  }
}
