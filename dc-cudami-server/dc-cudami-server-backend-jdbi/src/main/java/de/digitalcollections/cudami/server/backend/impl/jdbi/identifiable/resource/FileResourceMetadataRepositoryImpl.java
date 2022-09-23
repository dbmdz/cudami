package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.SearchTermTemplates;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
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

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", filename, http_base_url, mimetype, size_in_bytes, uri";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :filename, :httpBaseUrl, :mimeType, :sizeInBytes, :uri";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  @Override
  public String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectReducedFields(tableAlias, mappingPrefix)
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

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues()
        + ", filename=:filename, http_base_url=:httpBaseUrl, mimetype=:mimeType, size_in_bytes=:sizeInBytes, uri=:uri";
  }

  @Autowired
  public FileResourceMetadataRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        FileResource.class,
        cudamiConfig.getOffsetForAlternativePaging());
  }

  protected FileResourceMetadataRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class fileResourceImplClass,
      int offsetForAlternativePaging) {
    super(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        fileResourceImplClass,
        offsetForAlternativePaging);
  }

  protected FileResourceMetadataRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      String sqlSelectAllFieldsJoins,
      BiFunction<Map<UUID, F>, RowView, Map<UUID, F>> additionalReduceRowsBiFunction,
      int offsetForAlternativePaging) {
    super(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        FileResource.class,
        sqlSelectAllFieldsJoins,
        additionalReduceRowsBiFunction,
        offsetForAlternativePaging);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("filename", "mimeType", "sizeInBytes"));
    return allowedOrderByFields;
  }

  @Override
  public String getColumnName(String modelProperty) {
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
      case "uri":
        return tableAlias + ".uri";
      default:
        return null;
    }
  }

  @Override
  protected List<String> getSearchTermTemplates(String tblAlias, String originalSearchTerm) {
    if (originalSearchTerm == null) {
      return Collections.EMPTY_LIST;
    }
    List<String> searchTermTemplates = super.getSearchTermTemplates(tblAlias, originalSearchTerm);
    searchTermTemplates.add(SearchTermTemplates.ILIKE_SEARCH.renderTemplate(tblAlias, "filename"));
    return searchTermTemplates;
  }

  @Override
  public F save(F fileResource) {
    super.save(fileResource);
    F result = getByUuid(fileResource.getUuid());
    return result;
  }

  @Override
  protected boolean supportsCaseSensitivityForProperty(String modelProperty) {
    switch (modelProperty) {
      case "filename":
      case "mimeType":
        return true;
      default:
        return super.supportsCaseSensitivityForProperty(modelProperty);
    }
  }

  @Override
  public F update(F fileResource) {
    super.update(fileResource);
    F result = getByUuid(fileResource.getUuid());
    return result;
  }
}
