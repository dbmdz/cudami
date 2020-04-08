package de.digitalcollections.cudami.server.backend.impl.database.migration;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

@SuppressWarnings("checkstyle:typename")
public class V1_6_3__DML_Change_fileresource_filename_to_resource extends BaseJavaMigration {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(V1_6_3__DML_Change_fileresource_filename_to_resource.class);

  @Override
  public void migrate(Context context) throws Exception {
    JdbcTemplate jdbcTemplate =
        new JdbcTemplate(
            new SingleConnectionDataSource(context.getConnection(), true).getConnection());

    String selectQuery = "SELECT uuid, uri FROM fileresources";
    String updateQuery = "UPDATE fileresources SET uri=? WHERE uuid=?::uuid";

    List<Map<String, String>> identifiables = jdbcTemplate.queryForList(selectQuery);
    for (Map<String, String> identifiable : identifiables) {
      final String currentUri = identifiable.get("uri");
      if (currentUri != null) {
        String newUri = convertUri(currentUri);
        jdbcTemplate.update(updateQuery, newUri, identifiable.get("uuid"));
        // rename file on disk
        Path source = Paths.get(URI.create(currentUri));
        Path target = Paths.get(URI.create(newUri));
        try {
          Files.move(source, target, REPLACE_EXISTING);
        } catch (NoSuchFileException e) {
          LOGGER.warn("File not found at " + currentUri);
        }
      }
    }
  }

  protected String convertUri(String currentUri) {
    String basePath = currentUri.substring(0, currentUri.lastIndexOf("/"));
    String filename = currentUri.substring(currentUri.lastIndexOf("/") + 1);
    String extension = null;
    if (filename.contains(".")) {
      extension = filename.substring(filename.lastIndexOf("."));
    }
    String newUri = basePath + "/resource";
    if (extension != null) {
      newUri = newUri + extension;
    }
    return newUri;
  }
}
