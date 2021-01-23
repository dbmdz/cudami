package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileResourceMetadataRepositoryImpl extends IdentifiableRepositoryImpl<FileResource>
    implements FileResourceMetadataRepository<FileResource> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataRepositoryImpl.class);
  public static final String MAPPING_PREFIX = "fr";
  public static final String SQL_PREVIEW_IMAGE_FIELDS_PI =
      " file.uuid pi_uuid, file.filename pi_filename, file.mimetype pi_mimeType, file.uri pi_uri, file.http_base_url pi_httpBaseUrl";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "fileresources";

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return IdentifiableRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".filename "
        + mappingPrefix
        + "_filename, "
        + tableAlias
        + ".http_base_url "
        + mappingPrefix
        + "_httpBaseUrl, "
        + tableAlias
        + ".mimetype "
        + mappingPrefix
        + "_mimeType, "
        + tableAlias
        + ".size_in_bytes "
        + mappingPrefix
        + "_sizeInBytes, "
        + tableAlias
        + ".uri "
        + mappingPrefix
        + "_uri";
  }

  @Autowired
  public FileResourceMetadataRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    super(
        dbi, identifierRepository, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, FileResourceImpl.class);
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public SearchPageResponse<FileResource> find(SearchPageRequest searchPageRequest) {
    String commonSql = getCommonFileResourceSearchSql(tableName, tableAlias);
    return find(searchPageRequest, commonSql, Map.of("searchTerm", searchPageRequest.getQuery()));
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "filename", "lastModified", "sizeInBytes"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "filename":
        return tableAlias + ".filename";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "sizeInBytes":
        return tableAlias + ".size_in_bytes";
      default:
        return null;
    }
  }

  protected String getCommonFileResourceColumnsSql() {
    return "uuid, label, description, previewfileresource, preview_hints, custom_attrs, identifiable_type, created, last_modified, filename, mimetype, size_in_bytes, uri, http_base_url";
  }

  protected String getCommonFileResourcePropertiesSql() {
    return ":uuid, :label::JSONB, :description::JSONB, :previewFileResource, :previewImageRenderingHints::JSONB, :customAttributes::JSONB, :type, :created, :lastModified, :filename, :mimeType, :sizeInBytes, :uri, :httpBaseUrl";
  }

  public String getCommonFileResourceSearchSql(String tableName, String tableAlias) {
    String commonSql =
        " FROM "
            + tableName
            + " AS "
            + tableAlias
            + " LEFT JOIN LATERAL jsonb_object_keys("
            + tableAlias
            + ".label) l(keys) ON "
            + tableAlias
            + ".label IS NOT NULL"
            + " LEFT JOIN LATERAL jsonb_object_keys("
            + tableAlias
            + ".description) d(keys) ON "
            + tableAlias
            + ".description IS NOT NULL"
            + " WHERE ("
            + tableAlias
            + ".label->>l.keys ILIKE '%' || :searchTerm || '%'"
            + " OR "
            + tableAlias
            + ".description->>d.keys ILIKE '%' || :searchTerm || '%'"
            + " OR "
            + tableAlias
            + ".filename ILIKE '%' || :searchTerm || '%')";
    return commonSql;
  }

  protected String getCommonFileResourceUpdateSql() {
    return "label=:label::JSONB, description=:description::JSONB,"
        + " previewfileresource=:previewFileResource, preview_hints=:previewImageRenderingHints::JSONB, custom_attrs=:customAttributes::JSONB,"
        + " last_modified=:lastModified, http_base_url=:httpBaseUrl";
  }

  @Override
  public FileResource save(FileResource fileResource) {
    if (fileResource.getUuid() == null) {
      fileResource.setUuid(UUID.randomUUID());
    }
    if (fileResource.getLabel() == null && fileResource.getFilename() != null) {
      // set a default label = filename (an empty label violates constraint)
      fileResource.setLabel(new LocalizedTextImpl(Locale.ROOT, fileResource.getFilename()));
    }
    fileResource.setCreated(LocalDateTime.now());
    fileResource.setLastModified(LocalDateTime.now());
    final UUID previewImageUuid =
        fileResource.getPreviewImage() == null ? null : fileResource.getPreviewImage().getUuid();

    final String sql =
        "INSERT INTO "
            + tableName
            + "("
            + getCommonFileResourceColumnsSql()
            + ") VALUES ("
            + getCommonFileResourcePropertiesSql()
            + ")";

    dbi.withHandle(
        h ->
            h.createUpdate(sql)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(fileResource)
                .execute());

    // save identifiers
    Set<Identifier> identifiers = fileResource.getIdentifiers();
    saveIdentifiers(identifiers, fileResource);

    FileResource result = findOne(fileResource.getUuid());
    return result;
  }

  @Override
  public FileResource update(FileResource fileResource) {
    fileResource.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    final UUID previewImageUuid =
        fileResource.getPreviewImage() == null ? null : fileResource.getPreviewImage().getUuid();

    String query =
        "UPDATE " + tableName + " SET " + getCommonFileResourceUpdateSql() + " WHERE uuid=:uuid";
    dbi.withHandle(
        h ->
            h.createUpdate(query)
                .bind("previewFileResource", previewImageUuid)
                .bindBean(fileResource)
                .execute());

    // save identifiers
    // as we store the whole list new: delete old entries
    deleteIdentifiers(fileResource.getUuid());
    Set<Identifier> identifiers = fileResource.getIdentifiers();
    saveIdentifiers(identifiers, fileResource);

    FileResource result = findOne(fileResource.getUuid());
    return result;
  }
}
