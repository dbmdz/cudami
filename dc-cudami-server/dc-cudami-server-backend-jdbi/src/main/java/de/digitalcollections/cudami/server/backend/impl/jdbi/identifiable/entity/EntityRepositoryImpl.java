package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.RowView;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends Entity> extends IdentifiableRepositoryImpl<E>
    implements EntityRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityRepositoryImpl.class);

  public static final String MAPPING_PREFIX = "e";
  public static final String TABLE_ALIAS = "e";
  public static final String TABLE_NAME = "entities";

  public static String getSqlInsertFields() {
    return IdentifiableRepositoryImpl.getSqlInsertFields() + ", custom_attrs, entity_type, navdate";
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  public static String getSqlInsertValues() {
    // FIXME add navdate!
    // refid is generated as serial, DO NOT SET!
    return IdentifiableRepositoryImpl.getSqlInsertValues()
        + ", :customAttributes::JSONB, :entityType";
  }

  public static String getSqlSelectAllFields(String tableAlias, String mappingPrefix) {
    return getSqlSelectReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlSelectReducedFields(String tableAlias, String mappingPrefix) {
    return IdentifiableRepositoryImpl.getSqlSelectReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".custom_attrs "
        + mappingPrefix
        + "_customAttributes, "
        + tableAlias
        + ".navdate "
        + mappingPrefix
        + "_navDate, "
        + tableAlias
        + ".entity_type "
        + mappingPrefix
        + "_entityType, "
        + tableAlias
        + ".refid "
        + mappingPrefix
        + "_refId";
  }

  public static String getSqlUpdateFieldValues() {
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, entity_type, refid
    return IdentifiableRepositoryImpl.getSqlUpdateFieldValues()
        + ", custom_attrs=:customAttributes::JSONB";
  }

  private FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl;

  @Autowired
  private EntityRepositoryImpl(
      Jdbi dbi,
      FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl,
      CudamiConfig cudamiConfig) {
    this(
        dbi,
        TABLE_NAME,
        TABLE_ALIAS,
        MAPPING_PREFIX,
        Entity.class,
        getSqlSelectAllFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlSelectReducedFields(TABLE_ALIAS, MAPPING_PREFIX),
        getSqlInsertFields(),
        getSqlInsertValues(),
        getSqlUpdateFieldValues(),
        cudamiConfig.getOffsetForAlternativePaging());
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
  }

  protected EntityRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class entityImplClass,
      String sqlSelectAllFields,
      String sqlSelectReducedFields,
      String sqlInsertFields,
      String sqlInsertValues,
      String sqlUpdateFieldValues,
      int offsetForAlternativePaging) {
    this(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        entityImplClass,
        sqlSelectAllFields,
        sqlSelectReducedFields,
        sqlInsertFields,
        sqlInsertValues,
        sqlUpdateFieldValues,
        null,
        offsetForAlternativePaging);
  }

  protected EntityRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class entityImplClass,
      String sqlSelectAllFields,
      String sqlSelectReducedFields,
      String sqlInsertFields,
      String sqlInsertValues,
      String sqlUpdateFieldValues,
      String sqlSelectAllFieldsJoins,
      int offsetForAlternativePaging) {
    this(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        entityImplClass,
        sqlSelectAllFields,
        sqlSelectReducedFields,
        sqlInsertFields,
        sqlInsertValues,
        sqlUpdateFieldValues,
        sqlSelectAllFieldsJoins,
        null,
        offsetForAlternativePaging);
  }

  protected EntityRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class entityImplClass,
      String sqlSelectAllFields,
      String sqlSelectReducedFields,
      String sqlInsertFields,
      String sqlInsertValues,
      String sqlUpdateFieldValues,
      String sqlSelectAllFieldsJoins,
      BiFunction<Map<UUID, E>, RowView, Map<UUID, E>> additionalReduceRowsBiFunction,
      int offsetForAlternativePaging) {
    super(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        entityImplClass,
        sqlSelectAllFields,
        sqlSelectReducedFields,
        sqlInsertFields,
        sqlInsertValues,
        sqlUpdateFieldValues,
        sqlSelectAllFieldsJoins,
        additionalReduceRowsBiFunction,
        offsetForAlternativePaging);
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    Integer nextSortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "rel_entity_fileresources", "entity_uuid", entityUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortindex) VALUES (:entity_uuid, :fileresource_uuid, :sortindex)")
                .bind("entity_uuid", entityUuid)
                .bind("fileresource_uuid", fileResourceUuid)
                .bind("sortindex", nextSortIndex)
                .execute());
  }

  @Override
  public E getByRefId(long refId) {
    Filtering filtering = Filtering.defaultBuilder().filter("refId").isEquals(refId).build();

    return retrieveOne(sqlSelectAllFields, sqlSelectAllFieldsJoins, filtering);
  }

  @Override
  public List<E> findRandom(int count) {
    // Warning: could be very slow if random is used on tables with many million records
    // see https://www.gab.lc/articles/bigdata_postgresql_order_by_random/
    StringBuilder innerQuery =
        new StringBuilder("SELECT * FROM " + tableName + " ORDER BY RANDOM() LIMIT " + count);
    return retrieveList(sqlSelectReducedFields, innerQuery, null, null);
  }

  @Override
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("entityType", "refId"));
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
      case "entityType":
        return tableAlias + ".entity_type";
      case "refId":
        return tableAlias + ".refid";
      default:
        return null;
    }
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityUuid) {
    final String frTableAlias = fileResourceMetadataRepositoryImpl.getTableAlias();
    final String frTableName = fileResourceMetadataRepositoryImpl.getTableName();

    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT ref.sortindex AS idx, * FROM "
                + frTableName
                + " AS "
                + frTableAlias
                + " INNER JOIN rel_entity_fileresources ref ON "
                + frTableAlias
                + ".uuid = ref.fileresource_uuid"
                + " WHERE ref.entity_uuid = :entityUuid"
                + " ORDER BY idx ASC");

    Map<String, Object> argumentMappings = new HashMap<>();
    argumentMappings.put("entityUuid", entityUuid);

    return fileResourceMetadataRepositoryImpl.retrieveList(
        fileResourceMetadataRepositoryImpl.getSqlSelectReducedFields(),
        innerQuery,
        argumentMappings,
        "ORDER BY idx ASC");
  }

  @Override
  public E save(E entity, Map<String, Object> bindings) {
    return super.save(entity, bindings);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID entityUuid, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM rel_entity_fileresources WHERE entity_uuid = :uuid")
                .bind("uuid", entityUuid)
                .execute());

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
          for (FileResource fileResource : fileResources) {
            preparedBatch
                .bind("uuid", entityUuid)
                .bind("fileResourceUuid", fileResource.getUuid())
                .bind("sortIndex", getIndex(fileResources, fileResource))
                .add();
          }
          preparedBatch.execute();
        });
    return getRelatedFileResources(entityUuid);
  }

  @Override
  public E update(E entity, Map<String, Object> bindings) {
    return super.update(entity, bindings);
  }
}
