package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.SearchTermTemplates;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.Arrays;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class FileResourceMetadataRepositoryImpl<F extends FileResource>
    extends IdentifiableRepositoryImpl<F> implements FileResourceMetadataRepository<F> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "fr";
  public static final String SQL_PREVIEW_IMAGE_FIELDS_PI =
      " file.uuid pi_uuid, file.filename pi_filename, file.mimetype pi_mimeType, file.uri pi_uri, file.http_base_url pi_httpBaseUrl";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "fileresources";

  public static String getSqlInsertFields() {
    return IdentifiableRepositoryImpl.getSqlInsertFields()
        + ", filename, http_base_url, mimetype, size_in_bytes, uri";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return IdentifiableRepositoryImpl.getSqlInsertValues()
        + ", :filename, :httpBaseUrl, :mimeType, :sizeInBytes, :uri";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return IdentifiableRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
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

  public static String getSqlUpdateFieldValues() {
    return IdentifiableRepositoryImpl.getSqlUpdateFieldValues()
        + ", filename=:filename, http_base_url=:httpBaseUrl, mimetype=:mimeType, size_in_bytes=:sizeInBytes, uri=:uri";
  }

  @Autowired
  public FileResourceMetadataRepositoryImpl(Jdbi dbi) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        FileResource.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
  }

  protected FileResourceMetadataRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class fileResourceImplClass,
      String sqlSelectAllFields,
      String sqlSelectReducedFields,
      String sqlInsertFields,
      String sqlInsertValues,
      String sqlUpdateFieldValues) {
    super(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        fileResourceImplClass,
        sqlSelectAllFields,
        sqlSelectReducedFields,
        sqlInsertFields,
        sqlInsertValues,
        sqlUpdateFieldValues);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("filename", "mimeType", "sizeInBytes"));
    return allowedOrderByFields;
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    if (super.getColumnName(modelProperty) != null) {
      return super.getColumnName(modelProperty);
    }
    switch (modelProperty) {
      case "filename":
        return tableAlias + ".filename";
      case "mimeType":
        return tableAlias + ".mimetype";
      case "sizeInBytes":
        return tableAlias + ".size_in_bytes";
      default:
        return null;
    }
  }

  @Override
  protected List<String> getSearchTermTemplates(String tblAlias) {
    List<String> searchTermTemplates = super.getSearchTermTemplates(tblAlias);
    searchTermTemplates.add(SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tblAlias, "filename"));
    return searchTermTemplates;
  }

  @Override
  public F save(F fileResource) {
    super.save(fileResource);
    F result = findOne(fileResource.getUuid());
    return result;
  }

  @Override
  public F update(F fileResource) {
    super.update(fileResource);
    F result = findOne(fileResource.getUuid());
    return result;
  }
}
