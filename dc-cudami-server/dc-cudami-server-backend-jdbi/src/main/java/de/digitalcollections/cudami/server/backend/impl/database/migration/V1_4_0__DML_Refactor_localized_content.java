package de.digitalcollections.cudami.server.backend.impl.database.migration;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@SuppressWarnings("checkstyle:typename")
public class V1_4_0__DML_Refactor_localized_content extends BaseJavaMigration {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(V1_4_0__DML_Refactor_localized_content.class);

  @Override
  public void migrate(Context context) throws SQLException {
    JdbcTemplate jdbcTemplate =
        new JdbcTemplate(
            new SingleConnectionDataSource(context.getConnection(), true).getConnection());
    migrateLocalizedStructuredContent(jdbcTemplate, "description", "identifiables");
    migrateLocalizedStructuredContent(jdbcTemplate, "text", "articles");
    migrateLocalizedStructuredContent(jdbcTemplate, "text", "webpages");
    migrateLocalizedText(jdbcTemplate);
  }

  private String convertLocalizedStructuredContent(String currentJson) {
    JSONObject localizedStructuredContent =
        new JSONObject(currentJson).getJSONObject("localizedStructuredContent");
    JSONObject result = new JSONObject();
    localizedStructuredContent
        .keySet()
        .forEach(
            (locale) -> {
              result.put(locale, localizedStructuredContent.get(locale));
            });
    return result.toString();
  }

  private void migrateLocalizedStructuredContent(
      JdbcTemplate jdbcTemplate, String tableField, String tableName) throws SQLException {
    String selectQuery = String.format("SELECT uuid,%s FROM %s", tableField, tableName);
    String updateQuery =
        String.format("UPDATE %s SET %s=?::JSONB WHERE uuid=?::uuid", tableName, tableField);

    LOGGER.info("Migrating from select: " + selectQuery + " to update: " + updateQuery);

    List<Map<String, String>> identifiables = jdbcTemplate.queryForList(selectQuery);
    for (Map<String, String> identifiable : identifiables) {
      LOGGER.info("Migrating " + tableField + " of identifiable: " + identifiable.get("uuid"));
      final String currentJson = identifiable.get(tableField);
      if (currentJson != null) {
        jdbcTemplate.update(
            updateQuery, convertLocalizedStructuredContent(currentJson), identifiable.get("uuid"));
      }
    }
  }

  private String convertLocalizedText(String currentJson) {
    JSONArray labelTranslations = new JSONObject(currentJson).getJSONArray("translations");
    JSONObject result = new JSONObject();
    labelTranslations.forEach(
        (translation) -> {
          JSONObject currentTranslation = (JSONObject) translation;
          if (currentTranslation.has("text")) {
            result.put((String) currentTranslation.get("locale"), currentTranslation.get("text"));
          }
        });
    return result.toString();
  }

  private void migrateLocalizedText(JdbcTemplate jdbcTemplate) throws SQLException {
    String selectQuery = "SELECT label,uuid FROM identifiables";
    String updateQuery = "UPDATE identifiables SET label=?::JSONB WHERE uuid=?::uuid";

    List<Map<String, String>> identifiables = jdbcTemplate.queryForList(selectQuery);
    for (Map<String, String> identifiable : identifiables) {
      LOGGER.info("Migrating label of identifiable: " + identifiable.get("uuid"));
      final String currentJson = identifiable.get("label");
      if (currentJson != null) {
        jdbcTemplate.update(
            updateQuery, convertLocalizedText(currentJson), identifiable.get("uuid"));
      }
    }
  }
}
