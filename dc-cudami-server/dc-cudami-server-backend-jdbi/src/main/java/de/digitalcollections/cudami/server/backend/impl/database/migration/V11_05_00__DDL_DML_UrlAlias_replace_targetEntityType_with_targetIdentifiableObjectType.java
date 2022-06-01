package de.digitalcollections.cudami.server.backend.impl.database.migration;

import com.github.openjson.JSONObject;
import de.digitalcollections.model.identifiable.IdentifiableObjectType;
import java.util.List;
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
  private JdbcTemplate jdbcTemplate;

  @Override
  public void migrate(Context context) throws Exception {
    final SingleConnectionDataSource connectionDataSource =
        new SingleConnectionDataSource(context.getConnection(), true);

    // do setup
    jdbcTemplate = new JdbcTemplate(connectionDataSource);

    // select all needed data for migration
    String selectQuery =
        "SELECT identifiables.uuid AS target_uuid, identifiable_objecttype FROM identifiables INNER JOIN (SELECT DISTINCT target_uuid FROM url_aliases) url_aliases ON identifiables.uuid=url_aliases.target_uuid";
    List<Map<String, Object>> targetData = jdbcTemplate.queryForList(selectQuery);

    if (targetData.isEmpty()) {
      LOGGER.info("No migration necessary.");
      return;
    }

    LOGGER.info("Migrating UrlAliases for {} identifiables", targetData.size());

    try {
      // Disable all triggers for faster updates
      jdbcTemplate.execute("ALTER TABLE url_aliases DISABLE TRIGGER tr_url_aliases_target_uuid");

      // Do the actual migration
      targetData.forEach(
          i -> {
            JSONObject jsonObject = new JSONObject(i.toString());

            UUID targetUuid = UUID.fromString(jsonObject.getString("target_uuid"));
            IdentifiableObjectType targetIdentifiableObjectType =
                IdentifiableObjectType.valueOf(jsonObject.getString("identifiable_objecttype"));

            jdbcTemplate.update(
                "UPDATE url_aliases SET target_identifiable_objecttype = ? WHERE target_uuid = ?",
                targetIdentifiableObjectType.toString(),
                targetUuid);
          });
      LOGGER.info("Migration done");
    } finally {
      // Re-enable the triggers
      jdbcTemplate.execute("ALTER TABLE url_aliases ENABLE TRIGGER tr_url_aliases_target_uuid");
    }
  }
}
