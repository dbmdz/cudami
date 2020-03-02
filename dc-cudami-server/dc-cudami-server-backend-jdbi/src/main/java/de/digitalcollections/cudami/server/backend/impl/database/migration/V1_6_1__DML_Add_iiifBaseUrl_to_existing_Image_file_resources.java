package de.digitalcollections.cudami.server.backend.impl.database.migration;

import de.digitalcollections.cudami.server.config.SpringConfigBackendDatabase;
import java.net.URL;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@SuppressWarnings("checkstyle:typename")
public class V1_6_1__DML_Add_iiifBaseUrl_to_existing_Image_file_resources
    extends BaseJavaMigration {

  @Override
  public void migrate(Context context) throws Exception {
    URL iiifImageBaseUrl = SpringConfigBackendDatabase.iiifImageBaseUrl.get();
    String updateQuery = "UPDATE fileresources_image SET iiif_base_url=? WHERE uri LIKE 'file:%'";
    JdbcTemplate jdbcTemplate =
        new JdbcTemplate(
            new SingleConnectionDataSource(context.getConnection(), true).getConnection());
    jdbcTemplate.update(updateQuery, iiifImageBaseUrl);
  }
}
