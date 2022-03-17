package de.digitalcollections.cudami.server.backend.impl.database.migration;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class V9_05_01__DDL_Add_missing_column_to_predicates extends BaseJavaMigration {

  @Override
  public void migrate(Context context) throws Exception {
    JdbcTemplate jdbcTemplate =
        new JdbcTemplate(
            new SingleConnectionDataSource(context.getConnection(), true).getConnection());

    // Add new column "uuid"
    jdbcTemplate.executeStatement("ALTER TABLE predicates ADD COLUMN IF NOT EXISTS uuid UUID");

    List<Map<String, String>> predicates =
        jdbcTemplate.queryForList("SELECT uuid, value FROM predicates");
    for (Map<String, String> predicate : predicates) {
      // Add random value for new column "uuid"
      jdbcTemplate.update(
          "UPDATE predicates SET uuid=?::uuid WHERE value=?",
          UUID.randomUUID().toString(),
          predicate.get("value"));
    }

    // Disable null values for new colum "uuid"
    jdbcTemplate.executeStatement("ALTER TABLE predicates ALTER COLUMN uuid SET NOT NULL");

    // Make new column "uuid" unique
    jdbcTemplate.executeStatement("ALTER TABLE predicates ADD UNIQUE (uuid)");
  }
}
