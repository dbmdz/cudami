package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.model.api.identifiable.parts.LocalizedText;
import de.digitalcollections.model.api.identifiable.parts.structuredcontent.LocalizedStructuredContent;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.impl.identifiable.resource.ApplicationFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.AudioFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.TextFileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.VideoFileResourceImpl;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMappers;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class FileResourceMapper implements RowMapper<FileResource> {

  @Override
  public FileResource map(ResultSet rs, StatementContext ctx) throws SQLException {
    ConfigRegistry config = ctx.getConfig();
    ColumnMappers columnMappers = config.get(ColumnMappers.class);
    ColumnMapper<LocalizedStructuredContent> lscMapper = columnMappers.findFor(LocalizedStructuredContent.class).orElseThrow(() -> new NoSuchMapperException("LocalizedStructuredContent"));
    ColumnMapper<LocalizedText> ltMapper = columnMappers.findFor(LocalizedText.class).orElseThrow(() -> new NoSuchMapperException("LocalizedText"));

    FileResource result;
    String mimeType = rs.getString("mimetype");
    String primaryType = "application";
    if (mimeType != null) {
      primaryType = mimeType.substring(0, mimeType.indexOf("/"));
    }
    switch (primaryType) {
      case "audio":
        result = new AudioFileResourceImpl();
        break;
      case "image":
        result = new ImageFileResourceImpl();
        break;
      case "text":
        result = new TextFileResourceImpl();
        break;
      case "video":
        result = new VideoFileResourceImpl();
        break;
      default:
        result = new ApplicationFileResourceImpl();
    }
    // identifiable columns
    result.setCreated(rs.getTimestamp("created").toLocalDateTime());
    result.setDescription(lscMapper.map(rs, "description", ctx));
    result.setLabel(ltMapper.map(rs, "label", ctx));
    result.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
    result.setUuid(rs.getObject("uuid", UUID.class));

    // fileresource columns
    result.setFilename(rs.getString("filename"));
    result.setMimeType(MimeType.fromTypename(mimeType));
    result.setSizeInBytes(rs.getLong("size_in_bytes"));
    String uriStr = rs.getString("uri");
    if (uriStr != null) {
      result.setUri(URI.create(uriStr));
    }
    /*
    //Caused by: java.sql.SQLFeatureNotSupportedException: Method org.postgresql.jdbc.PgResultSet.getURL(int) is not yet implemented.
    final URL url = rs.getURL("uri");
    if (url != null) {
      try {
        result.setUri(url.toURI());
      } catch (URISyntaxException ex) {
        throw new SQLException("invalid url" + "'" + url + "'", ex);
      }
    }
     */
    return result;
  }
}
