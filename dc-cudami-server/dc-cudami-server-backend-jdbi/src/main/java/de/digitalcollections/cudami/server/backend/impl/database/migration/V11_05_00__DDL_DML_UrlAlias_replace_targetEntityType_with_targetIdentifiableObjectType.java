package de.digitalcollections.cudami.server.backend.impl.database.migration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openjson.JSONObject;
import de.digitalcollections.commons.web.SlugGenerator;
import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.config.SpringUtility;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.alias.UrlAlias;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@SuppressWarnings("checkstyle:typename")
public class V11_05_00__DDL_DML_UrlAlias_replace_targetEntityType_with_targetIdentifiableObjectType
    extends BaseJavaMigration {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(
          V11_05_00__DDL_DML_UrlAlias_replace_targetEntityType_with_targetIdentifiableObjectType
              .class);

  private int countInserted = 0;
  private int countMigrated = 0;
  private final CudamiConfig cudamiConfig = SpringUtility.getBean(CudamiConfig.class);
  private List<String> excludedIdentifiableObjectTypes;
  private JdbcTemplate jdbcTemplate;
  private final SlugGenerator slugGenerator = new SlugGenerator();

  @Override
  public void migrate(Context context) throws Exception {
    final SingleConnectionDataSource connectionDataSource =
        new SingleConnectionDataSource(context.getConnection(), true);

    // do setup
    slugGenerator.setMaxLength(cudamiConfig.getUrlAlias().getMaxLength());
    if (cudamiConfig.getUrlAlias() != null
        && cudamiConfig.getUrlAlias().getGenerationExcludes() != null) {
      excludedIdentifiableObjectTypes = cudamiConfig.getUrlAlias().getGenerationExcludes();
      LOGGER.info("Excluding UrlAlias generation for " + excludedIdentifiableObjectTypes);
    }
    jdbcTemplate = new JdbcTemplate(connectionDataSource);

    // migrate all identifiables` urlaliasses
    String selectQuery =
        "SELECT uuid, label, identifiable_type, identifiable_objecttype FROM identifiables";
    List<Map<String, Object>> identifiables = jdbcTemplate.queryForList(selectQuery);

    if (identifiables.isEmpty()) {
      LOGGER.info("No Identifiables found. No migration necessary.");
      return;
    }

    LOGGER.info("Migrating {} identifiables (number before exclusions)", identifiables.size());
    identifiables.forEach(
        i -> {
          JSONObject jsonObject = new JSONObject(i.toString());
          IdentifiableObjectType identifiableObjectType =
              IdentifiableObjectType.valueOf(jsonObject.getString("identifiable_objecttype"));

          if (notExcluded(identifiableObjectType)) {
            migrateOrInsertUrlAliasForIdentifiable(jsonObject, identifiableObjectType);
          }
        });
    LOGGER.info("Migrated {}, inserted {} UrlAliases", countMigrated, countInserted);
  }

  private void migrateOrInsertUrlAliasForIdentifiable(
      JSONObject identifiableJson, IdentifiableObjectType identifiableObjectType) {
    UUID uuid = UUID.fromString(identifiableJson.getString("uuid"));

    // try update (if exists)
    int updateCount =
        jdbcTemplate.update(
            "UPDATE url_aliases SET target_identifiable_objecttype = ? WHERE target_uuid = ?",
            identifiableObjectType.toString(),
            uuid);

    if (updateCount == 0) {
      // as webpages has been migrated all before, it is an error if a webpage urlalias should be
      // inserted:
      if (IdentifiableObjectType.WEBPAGE == identifiableObjectType) {
        throw new IllegalStateException(
            "No new Webpage UrlAliasses expected! Illegal state of data.");
      }

      // create new UrlAlias and do insert
      try {
        Map<String, String> labels =
            new ObjectMapper().readValue(identifiableJson.getString("label"), HashMap.class);
        labels.forEach(
            (language, label) -> {
              Locale locale = Locale.forLanguageTag(language);
              String baseSlug = slugGenerator.generateSlug(label);
              String slug = baseSlug;
              int suffix = 0;
              while (true) {
                try {
                  if (!slugAlreadyExists(slug, locale)) {
                    break;
                  }
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
                    "{}: Building slug with suffix={}", identifiableObjectType.toString(), slug);
              }

              IdentifiableType identifiableType =
                  IdentifiableType.valueOf(identifiableJson.getString("identifiable_type"));

              UrlAlias urlAlias = new UrlAlias();
              urlAlias.setUuid(UUID.randomUUID());
              urlAlias.setTargetLanguage(locale);
              urlAlias.setPrimary(true);
              urlAlias.setTargetUuid(uuid);
              urlAlias.setTargetIdentifiableType(identifiableType);
              urlAlias.setTargetIdentifiableObjectType(identifiableObjectType);
              urlAlias.setSlug(slug);

              try {
                saveUrlAlias(urlAlias);
              } catch (SQLException e) {
                throw new RuntimeException("Cannot save urlAlias " + urlAlias + ": " + e, e);
              }
            });
        countInserted++;
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Cannot parse " + identifiableJson + ": " + e, e);
      }
    } else {
      countMigrated++;
    }
  }

  private boolean notExcluded(IdentifiableObjectType identifiableObjectType) {
    String exclusionKey = identifiableObjectType.toString();
    return !excludedIdentifiableObjectTypes.contains(exclusionKey);
  }

  private void saveUrlAlias(UrlAlias urlAlias) throws SQLException {
    String updateQuery =
        "INSERT INTO url_aliases (uuid,created,last_published,\"primary\",slug,target_identifiable_objecttype,target_identifiable_type,target_language,target_uuid,website_uuid) VALUES(?::uuid,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP,?,?,?,?,?,?::uuid,?::uuid);";
    jdbcTemplate.update(
        updateQuery,
        urlAlias.getUuid().toString(),
        urlAlias.isPrimary(),
        urlAlias.getSlug(),
        urlAlias.getTargetIdentifiableObjectType() != null
            ? urlAlias.getTargetIdentifiableObjectType().toString()
            : null,
        urlAlias.getTargetIdentifiableType().toString(),
        urlAlias.getTargetLanguage().toString(),
        urlAlias.getTargetUuid().toString(),
        null);
  }

  private boolean slugAlreadyExists(String slug, Locale locale) throws SQLException {
    int c =
        jdbcTemplate.queryForObject(
            "SELECT count(*) FROM url_aliases WHERE website_uuid IS NULL AND slug=? AND target_language=?",
            Integer.class,
            slug,
            locale.getLanguage());
    return c > 0;
  }
}
