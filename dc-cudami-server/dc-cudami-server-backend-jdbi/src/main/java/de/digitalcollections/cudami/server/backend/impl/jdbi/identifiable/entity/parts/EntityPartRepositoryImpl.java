package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.EntityPartRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.entity.parts.EntityPartImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EntityPartRepositoryImpl<P extends EntityPartImpl>
    extends IdentifiableRepositoryImpl<P> implements EntityPartRepository<P> {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityPartRepositoryImpl.class);

  protected EntityPartRepositoryImpl(
      Jdbi dbi,
      IdentifierRepository identifierRepository,
      String tableName,
      String tableAlias,
      String mappingPrefix,
      Class<P> entityPartImplClass,
      String reducedFieldsSql,
      String fullFieldsSql) {
    super(
        dbi,
        identifierRepository,
        tableName,
        tableAlias,
        mappingPrefix,
        entityPartImplClass,
        reducedFieldsSql,
        fullFieldsSql);
  }

  @Override
  public void addRelatedEntity(P entityPart, Entity entity) {
    addRelatedEntity(entityPart.getUuid(), entity.getUuid());
  }

  @Override
  public void addRelatedEntity(UUID entityPartUuid, UUID entityUuid) {
    Integer sortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "rel_entitypart_entities", "entitypart_uuid", entityPartUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO rel_entitypart_entities(entitypart_uuid, entity_uuid, sortindex) VALUES (:entitypart_uuid, :entity_uuid, :sortindex)")
                .bind("entitypart_uuid", entityPartUuid)
                .bind("entity_uuid", entityUuid)
                .bind("sortindex", sortIndex)
                .execute());
  }

  @Override
  public void addRelatedFileresource(P entityPart, FileResource fileResource) {
    addRelatedFileresource(entityPart.getUuid(), fileResource.getUuid());
  }

  @Override
  public void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid) {
    Integer sortIndex =
        retrieveNextSortIndexForParentChildren(
            dbi, "rel_entitypart_fileresources", "entitypart_uuid", entityPartUuid);

    dbi.withHandle(
        h ->
            h.createUpdate(
                    "INSERT INTO rel_entitypart_fileresources(entitypart_uuid, fileresource_uuid, sortindex) VALUES (:entitypart_uuid, :fileresource_uuid, :sortindex)")
                .bind("entitypart_uuid", entityPartUuid)
                .bind("fileresource_uuid", fileResourceUuid)
                .bind("sortindex", sortIndex)
                .execute());
  }

  @Override
  public List<Entity> getRelatedEntities(P entityPart) {
    return getRelatedEntities(entityPart.getUuid());
  }

  @Override
  public List<Entity> getRelatedEntities(UUID entityPartUuid) {
    String query =
        "SELECT * FROM entities e"
            + " INNER JOIN rel_entitypart_entities ref ON e.uuid=ref.entity_uuid"
            + " WHERE ref.entitypart_uuid = :entityPartUuid"
            + " ORDER BY ref.sortindex";

    List<EntityImpl> list =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("entityPartUuid", entityPartUuid)
                    .mapToBean(EntityImpl.class)
                    .list());
    List<Entity> result = list.stream().map(s -> (Entity) s).collect(Collectors.toList());
    return result;
  }

  @Override
  public List<FileResource> getRelatedFileResources(P entityPart) {
    return getRelatedFileResources(entityPart.getUuid());
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID entityPartUuid) {
    String query =
        "SELECT * FROM fileresources f"
            + " INNER JOIN rel_entitypart_fileresources ref ON f.uuid=ref.fileresource_uuid"
            + " WHERE ref.entitypart_uuid = :entityPartUuid"
            + " ORDER BY ref.sortindex";

    List<FileResource> result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("entityPartUuid", entityPartUuid)
                    .mapToBean(FileResourceImpl.class)
                    .map(FileResource.class::cast)
                    .list());
    return result;
  }

  @Override
  public List<Entity> saveRelatedEntities(P entityPart, List<Entity> entities) {
    return saveRelatedEntities(entityPart.getUuid(), entities);
  }

  @Override
  public List<Entity> saveRelatedEntities(UUID entityPartUuid, List<Entity> entities) {
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM rel_entitypart_entities WHERE entitypart_uuid = :uuid")
                .bind("uuid", entityPartUuid)
                .execute());

    if (entities != null) {
      // we assume that the entities are already saved...
      dbi.useHandle(
          handle -> {
            PreparedBatch preparedBatch =
                handle.prepareBatch(
                    "INSERT INTO rel_entitypart_entities(entitypart_uuid, entity_uuid, sortIndex) VALUES(:uuid, :entityUuid, :sortIndex)");
            for (Entity entity : entities) {
              preparedBatch
                  .bind("uuid", entityPartUuid)
                  .bind("entityUuid", entity.getUuid())
                  .bind("sortIndex", getIndex(entities, entity))
                  .add();
            }
            preparedBatch.execute();
          });
    }
    return getRelatedEntities(entityPartUuid);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      P entityPart, List<FileResource> fileResources) {
    return saveRelatedFileResources(entityPart.getUuid(), fileResources);
  }

  @Override
  public List<FileResource> saveRelatedFileResources(
      UUID entityPartUuid, List<FileResource> fileResources) {
    if (fileResources == null) {
      return null;
    }
    // as we store the whole list new: delete old entries
    dbi.withHandle(
        h ->
            h.createUpdate("DELETE FROM rel_entitypart_fileresources WHERE entitypart_uuid = :uuid")
                .bind("uuid", entityPartUuid)
                .execute());

    dbi.useHandle(
        handle -> {
          PreparedBatch preparedBatch =
              handle.prepareBatch(
                  "INSERT INTO rel_entity_fileresources(entitypart_uuid, fileresource_uuid, sortIndex) VALUES(:uuid, :fileResourceUuid, :sortIndex)");
          for (FileResource fileResource : fileResources) {
            preparedBatch
                .bind("uuid", entityPartUuid)
                .bind("fileResourceUuid", fileResource.getUuid())
                .bind("sortIndex", getIndex(fileResources, fileResource))
                .add();
          }
          preparedBatch.execute();
        });
    return getRelatedFileResources(entityPartUuid);
  }
}
