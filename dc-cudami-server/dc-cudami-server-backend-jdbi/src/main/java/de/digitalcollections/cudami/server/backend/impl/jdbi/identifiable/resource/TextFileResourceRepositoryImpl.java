package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.TextFileResourceRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.resource.TextFileResource;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.resource.TextFileResourceImpl;
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
public class TextFileResourceRepositoryImpl extends IdentifiableRepositoryImpl<TextFileResource>
    implements TextFileResourceRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(TextFileResourceRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "fr";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "fileresources_text";

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return FileResourceMetadataRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix);
  }

  private final FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl;

  @Autowired
  public TextFileResourceRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        TextFileResourceImpl.class);
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public SearchPageResponse<TextFileResource> find(SearchPageRequest searchPageRequest) {
    String commonSql =
        fileResourceMetadataRepositoryImpl.getCommonFileResourceSearchSql(tableName, tableAlias);
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

  @Override
  public TextFileResource save(TextFileResource fileResource) {
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
            + fileResourceMetadataRepositoryImpl.getCommonFileResourceColumnsSql()
            + ") VALUES ("
            + fileResourceMetadataRepositoryImpl.getCommonFileResourcePropertiesSql()
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

    TextFileResource result = findOne(fileResource.getUuid());
    return result;
  }

  @Override
  public TextFileResource update(TextFileResource fileResource) {
    fileResource.setLastModified(LocalDateTime.now());
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type
    final UUID previewImageUuid =
        fileResource.getPreviewImage() == null ? null : fileResource.getPreviewImage().getUuid();

    String query =
        "UPDATE "
            + tableName
            + " SET "
            + fileResourceMetadataRepositoryImpl.getCommonFileResourceUpdateSql()
            + " WHERE uuid=:uuid";
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

    TextFileResource result = findOne(fileResource.getUuid());
    return result;
  }
}
