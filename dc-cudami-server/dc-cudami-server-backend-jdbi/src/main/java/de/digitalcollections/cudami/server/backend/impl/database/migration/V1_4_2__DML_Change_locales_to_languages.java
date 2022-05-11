package de.digitalcollections.cudami.server.backend.impl.database.migration;

import com.github.openjson.JSONObject;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@SuppressWarnings("checkstyle:typename")
public class V1_4_2__DML_Change_locales_to_languages extends BaseJavaMigration {

  @Override
  public void migrate(Context context) throws SQLException {
    JdbcTemplate jdbcTemplate =
        new JdbcTemplate(
            new SingleConnectionDataSource(context.getConnection(), true).getConnection());
    migrateJson(jdbcTemplate, "description", "identifiables");
    migrateJson(jdbcTemplate, "label", "identifiables");
    migrateJson(jdbcTemplate, "text", "articles");
    migrateJson(jdbcTemplate, "text", "webpages");
  }

  private String convertJson(String currentJson) {
    JSONObject json = new JSONObject(currentJson);
    JSONObject result = new JSONObject();
    json.keySet()
        .forEach(
            (locale) -> {
              if (locale.contains("_")) {
                result.put(locale.substring(0, locale.indexOf("_")), json.get(locale));
              } else {
                result.put(locale, json.get(locale));
              }
            });
    return result.toString();
  }

  private void migrateJson(JdbcTemplate jdbcTemplate, String tableField, String tableName)
      throws SQLException {
    String selectQuery = String.format("SELECT uuid,%s FROM %s", tableField, tableName);
    String updateQuery =
        String.format("UPDATE %s SET %s=?::JSONB WHERE uuid=?::uuid", tableName, tableField);

    List<Map<String, String>> identifiables = jdbcTemplate.queryForList(selectQuery);
    for (Map<String, String> identifiable : identifiables) {
      final String currentJson = identifiable.get(tableField);
      if (currentJson != null) {
        jdbcTemplate.update(updateQuery, convertJson(currentJson), identifiable.get("uuid"));
      }
    }
  }
}
