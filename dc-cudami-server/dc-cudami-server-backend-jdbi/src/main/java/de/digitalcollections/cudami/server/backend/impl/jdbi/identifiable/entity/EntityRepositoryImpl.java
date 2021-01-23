package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import java.util.LinkedHashMap;
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

  public static String getSqlAllFields(String tableAlias, String mappingPrefix) {
    return getSqlReducedFields(tableAlias, mappingPrefix);
  }

  public static String getSqlReducedFields(String tableAlias, String mappingPrefix) {
    return IdentifiableRepositoryImpl.getSqlReducedFields(tableAlias, mappingPrefix)
        + ", "
        + tableAlias
        + ".custom_attrs "
        + mappingPrefix
        + "_customAttributes, "
        + tableAlias
        + ".entity_type "
        + mappingPrefix
        + "_entityType, "
        + tableAlias
        + ".refid "
        + mappingPrefix
        + "_refId";
  }

  private FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl;

  @Autowired
  private EntityRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      FileResourceMetadataRepositoryImpl fileResourceMetadataRepositoryImpl) {
    this(dbi, identifierRepository, TABLE_NAME, TABLE_ALIAS, MAPPING_PREFIX, EntityImpl.class);
    this.fileResourceMetadataRepositoryImpl = fileResourceMetadataRepositoryImpl;
    this.sqlAllFields = getSqlAllFields(tableAlias, mappingPrefix);
    this.sqlReducedFields = getSqlReducedFields(tableAlias, mappingPrefix);
  }

  protected EntityRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class entityImplClass) {
    this(dbi, identifierRepository, tableName, tableAlias, mappingPrefix, entityImplClass, null);
  }

  protected EntityRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class entityImplClass,
      String fullFieldsJoinsSql) {
    this(
        dbi,
        identifierRepository,
        tableName,
        tableAlias,
        mappingPrefix,
        entityImplClass,
        fullFieldsJoinsSql,
        null);
  }

  protected EntityRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class entityImplClass,
      String fullFieldsJoinsSql,
      BiFunction<LinkedHashMap<UUID, E>, RowView, LinkedHashMap<UUID, E>>
          additionalReduceRowsBiFunction) {
    super(
        dbi,
        identifierRepository,
        tableName,
        tableAlias,
        mappingPrefix,
        entityImplClass,
        fullFieldsJoinsSql,
        additionalReduceRowsBiFunction);
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
  public E findOneByRefId(long refId) {
    StringBuilder innerQuery =
        new StringBuilder(
            "SELECT * FROM "
                + tableName
                + " AS "
                + tableAlias
                + " WHERE "
                + tableAlias
                + ".refid = :refId");

    E result = retrieveOne(sqlAllFields, innerQuery, fullFieldsJoinsSql, Map.of("refId", refId));
    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[] {"created", "entityType", "lastModified", "refId", "type"};
  }

  @Override
  protected String getColumnName(String modelProperty) {
    if (modelProperty == null) {
      return null;
    }
    switch (modelProperty) {
      case "created":
        return tableAlias + ".created";
      case "entityType":
        return tableAlias + ".entity_type";
      case "lastModified":
        return tableAlias + ".last_modified";
      case "refId":
        return tableAlias + ".refid";
      case "type":
        return tableAlias + ".identifiable_type";
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
            "SELECT * FROM "
                + frTableName
                + " AS "
                + frTableAlias
                + " INNER JOIN rel_entity_fileresources ref ON "
                + frTableAlias
                + ".uuid = ref.fileresource_uuid"
                + " WHERE ref.entity_uuid = :entityUuid"
                + " ORDER BY ref.sortindex ASC");

    List<FileResource> result =
        fileResourceMetadataRepositoryImpl.retrieveList(
            fileResourceMetadataRepositoryImpl.getSqlReducedFields(),
            innerQuery,
            Map.of("entityUuid", entityUuid));

    return result;
  }

  @Override
  public E save(E entity) {
    throw new UnsupportedOperationException("Use save method of specific entity repository!");
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
  public E update(E entity) {
    throw new UnsupportedOperationException("Use update method of specific entity repo!");
  }
}
