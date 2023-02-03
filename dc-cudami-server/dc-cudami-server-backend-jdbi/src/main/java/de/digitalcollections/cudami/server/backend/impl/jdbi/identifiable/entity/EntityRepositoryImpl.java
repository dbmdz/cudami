package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.entity.NamedEntity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
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

  @Override
  public String getSqlInsertFields() {
    return super.getSqlInsertFields()
        + ", custom_attrs"
        + ", navdate"
        + ", notes"
        + (isRepoForNamedEntity() ? ", name, name_locales_original_scripts, split_name" : "");
  }

  /* Do not change order! Must match order in getSqlInsertFields!!! */
  @Override
  public String getSqlInsertValues() {
    // refid is generated as serial, DO NOT SET!
    return super.getSqlInsertValues()
        + ", :customAttributes::JSONB"
        + ", :navDate"
        + ", :notes::JSONB"
        + (isRepoForNamedEntity()
            ? ", :name::JSONB, :nameLocalesOfOriginalScripts::varchar[], :split_name::varchar[]"
            : "");
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
        + ".custom_attrs "
        + mappingPrefix
        + "_customAttributes, "
        + tableAlias
        + ".navdate "
        + mappingPrefix
        + "_navDate, "
        + tableAlias
        + ".refid "
        + mappingPrefix
        + "_refId, "
        + tableAlias
        + ".notes "
        + mappingPrefix
        + "_notes"
        + (isRepoForNamedEntity()
            ? String.format(
                ", %1$s.name %2$s_name, %1$s.name_locales_original_scripts %2$s_nameLocalesOfOriginalScripts",
                tableAlias, mappingPrefix)
            : "");
  }

  @Override
  public String getSqlUpdateFieldValues() {
    // do not update/left out from statement (not changed since insert):
    // uuid, created, identifiable_type, identifiable_objecttype, refid
    return super.getSqlUpdateFieldValues()
        + ", custom_attrs=:customAttributes::JSONB, navdate=:navDate, notes=:notes::JSONB"
        + (isRepoForNamedEntity()
            ? ", name=:name::JSONB, name_locales_original_scripts=:nameLocalesOfOriginalScripts::varchar[], split_name=:split_name::varchar[]"
            : "");
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
        cudamiConfig.getOffsetForAlternativePaging());
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
  }

  protected EntityRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends Entity> entityImplClass,
      int offsetForAlternativePaging) {
    this(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        entityImplClass,
        null,
        offsetForAlternativePaging);
  }

  protected EntityRepositoryImpl(
      Jdbi dbi,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<? extends Entity> entityImplClass,
      BiConsumer<Map<UUID, E>, RowView> additionalReduceRowsBiConsumer,
      int offsetForAlternativePaging) {
    super(
        dbi,
        tableName,
        tableAlias,
        mappingPrefix,
        entityImplClass,
        additionalReduceRowsBiConsumer,
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
  protected List<String> getAllowedOrderByFields() {
    List<String> allowedOrderByFields = super.getAllowedOrderByFields();
    allowedOrderByFields.addAll(Arrays.asList("entityType", "refId"));
    return allowedOrderByFields;
  }

  @Override
  public E getByRefId(long refId) {
    Filtering filtering =
        Filtering.builder()
            .add(FilterCriterion.builder().withExpression("refId").isEquals(refId).build())
            .build();

    return retrieveOne(getSqlSelectAllFields(), filtering, null);
  }

  @Override
  public String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "navdate":
        return tableAlias + ".navdate";
      case "refId":
        return tableAlias + ".refid";
      case "notes":
        return tableAlias + ".notes";
      default:
        return super.getColumnName(modelProperty);
    }
  }

  @Override
  public List<E> getRandom(int count) {
    // Warning: could be very slow if random is used on tables with many million records
    // see https://www.gab.lc/articles/bigdata_postgresql_order_by_random/
    StringBuilder innerQuery =
        new StringBuilder("SELECT * FROM " + tableName + " ORDER BY RANDOM() LIMIT " + count);
    return retrieveList(getSqlSelectReducedFields(), innerQuery, null, null);
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

  /**
   * Specify if this repository handles an {@code Entity} subclass that implements the interface
   * {@code NamedEntity} so the additional fields are being added properly.
   */
  protected boolean isRepoForNamedEntity() {
    return NamedEntity.class.isAssignableFrom(identifiableImplClass);
  }

  @Override
  protected List<String> getReturnedFieldsOnInsertUpdate() {
    var fields = super.getReturnedFieldsOnInsertUpdate();
    fields.add("refid");
    return fields;
  }

  @Override
  protected void insertUpdateCallback(E identifiable, Map<String, Object> returnedFields) {
    super.insertUpdateCallback(identifiable, returnedFields);
    identifiable.setRefId(Long.parseLong(returnedFields.getOrDefault("refid", 0).toString()));
  }

  @Override
  public void save(
      E entity,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier) {
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }
    if (isRepoForNamedEntity()) {
      bindings.put("split_name", splitToArray(((NamedEntity) entity).getName()));
    }
    super.save(entity, bindings, sqlModifier);
  }

  @Override
  public List<FileResource> setRelatedFileResources(
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
  public void update(E entity, Map<String, Object> bindings) throws RepositoryException {
    update(entity, bindings, null);
  }

  @Override
  public void update(
      E entity,
      Map<String, Object> bindings,
      BiFunction<String, Map<String, Object>, String> sqlModifier) {
    if (bindings == null) {
      bindings = new HashMap<>(0);
    }
    if (isRepoForNamedEntity()) {
      bindings.put("split_name", splitToArray(((NamedEntity) entity).getName()));
    }
    super.update(entity, bindings, sqlModifier);
  }
}
