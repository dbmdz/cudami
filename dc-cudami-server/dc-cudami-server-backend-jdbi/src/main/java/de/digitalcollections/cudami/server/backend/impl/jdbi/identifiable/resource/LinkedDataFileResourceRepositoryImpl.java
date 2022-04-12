package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.LinkedDataFileResourceRepository;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
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
  public LinkedDataFileResourceRepositoryImpl(Jdbi dbi, CudamiConfig cudamiConfig) {
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
        getSqlUpdateFieldValues(),
        cudamiConfig.getOffsetForAlternativePaging());
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

  @Override
  public List<LinkedDataFileResource> getLinkedDataFileResourcesForDigitalObjectUuid(
      UUID digitalObjectUuid) {
    final String ldfrTableAlias = getTableAlias();
    final String ldfrTableName = getTableName();
    final String fieldsSql = getSqlSelectAllFields();
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT dl.sortindex as idx, * FROM "
                + ldfrTableName
                + " AS "
                + ldfrTableAlias
                + " INNER JOIN digitalobject_linkeddataresources AS dl ON "
                + ldfrTableAlias
                + ".uuid = dl.linkeddata_fileresource_uuid"
                + " WHERE dl.digitalobject_uuid = :uuid"
                + " ORDER by dl.sortindex ASC");
    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("uuid", digitalObjectUuid);

    List<LinkedDataFileResource> linkedDataFileResources =
        retrieveList(fieldsSql, innerQuery, argumentMappings, "ORDER BY idx ASC");

    return linkedDataFileResources;
  }

  public List<LinkedDataFileResource> saveLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate(
                    "DELETE FROM digitalobject_linkeddataresources WHERE digitalobject_uuid = :uuid")
                .bind("uuid", digitalObjectUuid)
                .execute());

    if (linkedDataFileResources != null) {
      // first save linked data resources
      for (LinkedDataFileResource linkedDataFileResource : linkedDataFileResources) {
        if (linkedDataFileResource.getUuid() == null) {
          save(linkedDataFileResource);
        }
      }

      // second: save relations to digital object
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO digitalobject_linkeddataresources(digitalobject_uuid, linkeddata_fileresource_uuid, sortIndex) VALUES(:uuid, :linkedDataFileResourceUuid, :sortIndex)");
            for (LinkedDataFileResource linkedDataFileResource : linkedDataFileResources) {
              preparedBatch
                  .bind("uuid", digitalObjectUuid)
                  .bind("linkedDataFileResourceUuid", linkedDataFileResource.getUuid())
                  .bind("sortIndex", getIndex(linkedDataFileResources, linkedDataFileResource))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getLinkedDataFileResourcesForDigitalObjectUuid(digitalObjectUuid);
  }
}
