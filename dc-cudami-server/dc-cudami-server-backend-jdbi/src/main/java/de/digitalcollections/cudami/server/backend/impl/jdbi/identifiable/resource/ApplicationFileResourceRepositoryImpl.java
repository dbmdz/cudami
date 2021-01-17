package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.paging.SearchPageRequest;
import de.digitalcollections.model.api.paging.SearchPageResponse;
import de.digitalcollections.model.impl.identifiable.parts.LocalizedTextImpl;
import de.digitalcollections.model.impl.identifiable.resource.ApplicationFileResourceImpl;
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
public class ApplicationFileResourceRepositoryImpl
    extends IdentifiableRepositoryImpl<ApplicationFileResourceImpl>
    implements FileResourceMetadataRepository<ApplicationFileResourceImpl> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(ApplicationFileResourceRepositoryImpl.class);

  public static final String SQL_REDUCED_FIELDS_FR =
      FileResourceMetadataRepositoryImpl.SQL_REDUCED_FIELDS_FR;

  public static final String SQL_FULL_FIELDS_FR = SQL_REDUCED_FIELDS_FR;

  public static final String TABLE_NAME = "fileresources_application";

  private final FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl;

  @Autowired
  public ApplicationFileResourceRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl) {
    super(
        dbi,
        identifierRepository,
        TABLE_NAME,
        "f",
        "fr",
        ApplicationFileResourceImpl.class,
        SQL_REDUCED_FIELDS_FR,
        SQL_FULL_FIELDS_FR);
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
  }

  @Override
  public SearchPageResponse<ApplicationFileResourceImpl> find(SearchPageRequest searchPageRequest) {
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
  public ApplicationFileResourceImpl save(ApplicationFileResourceImpl fileResource) {
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

    ApplicationFileResourceImpl result = findOne(fileResource.getUuid());
    return result;
  }

  @Override
  public ApplicationFileResourceImpl update(ApplicationFileResourceImpl fileResource) {
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

    ApplicationFileResourceImpl result = findOne(fileResource.getUuid());
    return result;
  }
}
