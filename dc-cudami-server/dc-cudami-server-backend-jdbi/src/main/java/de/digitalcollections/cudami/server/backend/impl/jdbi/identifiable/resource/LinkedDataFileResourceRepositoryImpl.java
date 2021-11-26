package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.LinkedDataFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.Arrays;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LinkedDataFileResourceRepositoryImpl
    extends FileResourceMetadataRepositoryImpl<LinkedDataFileResource>
    implements LinkedDataFileResourceRepository {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LinkedDataFileResourceRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "fr";
  public static final String TABLE_ALIAS = "f";
  public static final String TABLE_NAME = "fileresources_linkeddata";

  public static String getSqlInsertFields() {
    return FileResourceMetadataRepositoryImpl.getSqlInsertFields() + ", context, object_type";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    return FileResourceMetadataRepositoryImpl.getSqlInsertValues() + ", :context, :objectType";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix)
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

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return FileResourceMetadataRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlUpdateFieldValues() {
    return FileResourceMetadataRepositoryImpl.getSqlUpdateFieldValues()
        + ", context=:context, object_type=:objectType";
  }

  @Autowired
  public LinkedDataFileResourceRepositoryImpl(Jdbi dbi) {
    super(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        LinkedDataFileResource.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues());
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("context", "objectType"));
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
      case "context":
        return tableAlias + ".context";
      case "objectType":
        return tableAlias + ".object_type";
      default:
        return null;
    }
  }
}
