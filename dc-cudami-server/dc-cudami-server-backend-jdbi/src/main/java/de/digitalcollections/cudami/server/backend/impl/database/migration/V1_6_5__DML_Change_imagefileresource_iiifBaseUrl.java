package de.digitalcollections.cudami.server.backend.impl.database.migration;

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
public class V1_6_5__DML_Change_imagefileresource_iiifBaseUrl extends BaseJavaMigration {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(V1_6_5__DML_Change_imagefileresource_iiifBaseUrl.class);

  @Override
  public void migrate(Context context) throws SQLException {
    JdbcTemplate jdbcTemplate =
        new JdbcTemplate(
            new SingleConnectionDataSource(context.getConnection(), true).getConnection());

    String selectQuery = "SELECT uuid, uri, iiif_base_url FROM fileresources_image";
    String updateQuery = "UPDATE fileresources_image SET iiif_base_url=? WHERE uuid=?::uuid";

    List<Map<String, String>> imageFileResources = jdbcTemplate.queryForList(selectQuery);
    for (Map<String, String> imageFileResource : imageFileResources) {
      final String currentUri = imageFileResource.get("uri");
      String newIiifBaseUrl = null;
      if (currentUri != null && currentUri.startsWith("file")) {
        newIiifBaseUrl = imageFileResource.get("iiif_base_url");
        if (!newIiifBaseUrl.endsWith("/")) {
          newIiifBaseUrl += "/";
        }
        newIiifBaseUrl += imageFileResource.get("uuid");
      }

      jdbcTemplate.update(updateQuery, newIiifBaseUrl, imageFileResource.get("uuid"));
    }
  }
}
