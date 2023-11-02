package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.alias.UrlAliasRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.LinkedDataFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.Arrays;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

@Repository
public class LinkedDataFileResourceRepositoryImpl
    extends FileResourceMetadataRepositoryImpl<LinkedDataFileResource>
    implements LinkedDataFileResourceRepository {

  public static final String MAPPING_PREFIX = "fld";
  public static final String TABLE_ALIAS = "fld";
  public static final String TABLE_NAME = "fileresources_linkeddata";

  public LinkedDataFileResourceRepositoryImpl(
      Jdbi dbi,
      CudamiConfig cudamiConfig,
      IdentifierRepository identifierRepository,
      UrlAliasRepository urlAliasRepository) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        LinkedDataFileResource.class,
        cudamiConfig.getOffsetForAlternativePaging(),
        identifierRepository,
        urlAliasRepository);
  }

  @Override
  public LinkedDataFileResource create() throws RepositoryException {
    return new LinkedDataFileResource();
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("context", "objectType"));
    return allowedOrderByFields;
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "context":
        return tableAlias + ".context";
      case "objectType":
        return tableAlias + ".object_type";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  protected String getSqlInsertFields() {
    return super.getSqlInsertFields() + ", context, object_type";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  protected String getSqlInsertValues() {
    return super.getSqlInsertValues() + ", :context, :objectType";
  }

  @Override
  public String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return super.getSqlSelectAllFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".context "
        + mappingPrefix
        + "_context, "
        + tableAlias
        + ".object_type "
        + mappingPrefix
        + "_objectType";
  }

  @Override
  protected String getSqlUpdateFieldValues() {
    return super.getSqlUpdateFieldValues() + ", context=:context, object_type=:objectType";
  }
}
