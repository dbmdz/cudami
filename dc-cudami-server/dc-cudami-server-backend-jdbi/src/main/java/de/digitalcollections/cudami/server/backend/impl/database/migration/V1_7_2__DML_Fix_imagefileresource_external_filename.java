package de.digitalcollections.cudami.server.backend.impl.database.migration;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@SuppressWarnings("checkstyle:typename")
public class V1_7_2__DML_Fix_imagefileresource_external_filename extends BaseJavaMigration {

  @Override
  public void migrate(Context context) throws SQLException {
    JdbcTemplate jdbcTemplate =
        new JdbcTemplate(
            new SingleConnectionDataSource(context.getConnection(), true).getConnection());

    String selectQuery = "SELECT filename, uuid FROM fileresources_image";
    String updateQuery = "UPDATE fileresources_image SET filename=? WHERE uuid=?::uuid";

    List<Map<String, String>> imageFileResources = jdbcTemplate.queryForList(selectQuery);
    for (Map<String, String> imageFileResource : imageFileResources) {
      final String filename = imageFileResource.get("filename");
      if (filename.contains("/")) {
        jdbcTemplate.update(
            updateQuery,
            filename.substring(filename.lastIndexOf('/') + 1),
            imageFileResource.get("uuid"));
      }
    }
  }
}
