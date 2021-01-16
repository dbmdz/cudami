package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifierRepositoryImpl.SQL_FULL_IDENTIFIER_FIELDS_ID;
import static de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl.SQL_PREVIEW_IMAGE_FIELDS_PI;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.EntityRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import de.digitalcollections.model.impl.identifiable.resource.ImageFileResourceImpl;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityRepositoryImpl<E extends EntityImpl> extends IdentifiableRepositoryImpl<E>
        implements EntityRepository<E> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityRepositoryImpl.class);

  public static final String SQL_REDUCED_ENTITY_FIELDS_E
          = " e.uuid e_uuid, e.refid e_refId, e.label e_label, e.description e_description,"
          + " e.identifiable_type e_type, e.entity_type e_entityType,"
          + " e.created e_created, e.last_modified e_lastModified,"
          + " e.preview_hints e_previewImageRenderingHints";

  public static final String SQL_FULL_ENTITY_FIELDS_E = SQL_REDUCED_ENTITY_FIELDS_E;

  public static final String TABLE_NAME = "entities";
  
  @Autowired
  private EntityRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository) {
    this(dbi, identifierRepository, TABLE_NAME, "e", "e", (Class<E>) EntityImpl.class, SQL_REDUCED_ENTITY_FIELDS_E, SQL_FULL_ENTITY_FIELDS_E);
  }

  protected EntityRepositoryImpl(Jdbi dbi, IdentifierRepository identifierRepository, String tableName, String tableAlias, String mappingPrefix, Class<E> entityImplClass, String reducedFieldsSql, String fullFieldsSql) {
    super(dbi, identifierRepository, tableName, tableAlias, mappingPrefix, entityImplClass, reducedFieldsSql, fullFieldsSql);
  }

  @Override
  public void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid) {
    Integer nextSortIndex
            = dbi.withHandle(
                    (Handle h)
                    -> h.createQuery(
                            "SELECT MAX(sortIndex) + 1 FROM rel_entity_fileresources"
                            + " WHERE entity_uuid = :entityUuid")
                            .bind("entityUuid", entityUuid)
                            .mapTo(Integer.class)
                            .findOne()
                            .orElse(0));

    dbi.withHandle(
            h
            -> h.createUpdate(
                    "INSERT INTO rel_entity_fileresources(entity_uuid, fileresource_uuid, sortindex) VALUES (:entity_uuid, :fileresource_uuid, :sortindex)")
                    .bind("entity_uuid", entityUuid)
                    .bind("fileresource_uuid", fileResourceUuid)
                    .bind("sortindex", nextSortIndex)
                    .execute());
  }

  @Override
  public E findOneByRefId(long refId) {
    String innerQuery = "SELECT * FROM " + tableName + " AS " + tableAlias
            + " WHERE " + tableAlias + ".refid = :refId";

    final String sql
            = "SELECT"
            + fullFieldsSql
            + ","
            + SQL_FULL_IDENTIFIER_FIELDS_ID
            + ","
            + SQL_PREVIEW_IMAGE_FIELDS_PI
            + " FROM ("
            + innerQuery
            + ") AS " + tableAlias
            + " LEFT JOIN identifiers AS id ON " + tableAlias + ".uuid = id.identifiable"
            + " LEFT JOIN fileresources_image AS file ON " + tableAlias + ".previewfileresource = file.uuid";

    E result
            = dbi
                    .withHandle(
                            h
                            -> h.createQuery(sql)
                                    .bind("refId", refId)
                                    .registerRowMapper(BeanMapper.factory(identifiableImplClass, mappingPrefix))
                                    .registerRowMapper(BeanMapper.factory(IdentifierImpl.class, "id"))
                                    .registerRowMapper(BeanMapper.factory(ImageFileResourceImpl.class, "pi"))
                                    .reduceRows(new LinkedHashMap<UUID, E>(), mapRowToIdentifiable(true, true)))
                    .values()
                    .stream()
                    .findFirst()
                    .orElse(null);

    return result;
  }

  @Override
  protected String[] getAllowedOrderByFields() {
    return new String[]{"created", "entityType", "lastModified", "refId", "type"};
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
    String query
            = "SELECT * FROM fileresources f"
            + " INNER JOIN rel_entity_fileresources ref ON f.uuid=ref.fileresource_uuid"
            + " WHERE ref.entity_uuid = :entityUuid"
            + " ORDER BY ref.sortindex";

    List<FileResource> result
            = dbi.withHandle(
                    h
                    -> h
                            .createQuery(query)
                            .bind("entityUuid", entityUuid)
                            .mapToBean(FileResourceImpl.class)
                            .list()
                            .stream()
                            .map(FileResource.class::cast)
                            .collect(Collectors.toList()));
    return result;
  }

  @Override
  public E save(E entity) {
    throw new UnsupportedOperationException("Use save method of specific entity repository!");
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
          E entity, List<FileResource> fileResources) {
    return saveRelatedFileResources(entity.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
          UUID entityUuid, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    // as we store the whole list new: delete old entries
    dbi.withHandle(
            h
            -> h.createUpdate("DELETE FROM rel_entity_fileresources WHERE entity_uuid = :uuid")
                    .bind("uuid", entityUuid)
                    .execute());

    dbi.useHandle(
            handle -> {
              PreparedBatch preparedBatch
              = handle.prepareBatch(
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
