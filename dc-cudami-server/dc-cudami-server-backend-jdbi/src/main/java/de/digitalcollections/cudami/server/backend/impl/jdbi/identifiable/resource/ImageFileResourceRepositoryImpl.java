package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ImageFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import java.util.Arrays;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class ImageFileResourceRepositoryImpl
    extends FileResourceMetadataRepositoryImpl<ImageFileResource>
    implements ImageFileResourceRepository {

  public static final String MAPPING_PREFIX = "fr";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "fileresources_image";

  public ImageFileResourceRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        ImageFileResource.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public ImageFileResource create() throws RepositoryException {
    return new ImageFileResource();
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("height", "width"));
    return allowedOrderByFields;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "height":
        return tableAlias + ".height";
      case "width":
        return tableAlias + ".width";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", height, width";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :height, :width";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".height "
        + mappingPrefix
        + "_height, "
        + tableAlias
        + ".width "
        + mappingPrefix
        + "_width";
  }

  @Override
  public String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", height=:height, width=:width";
  }
}
