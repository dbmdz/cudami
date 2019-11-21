package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.parts;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.parts.EntityPartRepository;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.IdentifiableRepositoryImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.impl.identifiable.entity.EntityImpl;
import de.digitalcollections.model.impl.identifiable.resource.FileResourceImpl;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EntityPartRepositoryImpl<P extends EntityPart, E extends Entity>
    extends IdentifiableRepositoryImpl<P> implements EntityPartRepository<P, E> {

  @Autowired
  public EntityPartRepositoryImpl(Jdbi dbi) {
    super(dbi);
  }

  @Override
  public void addRelatedEntity(P entityPart, E entity) {
    addRelatedEntity(entityPart.getUuid(), entity.getUuid());
  }

  @Override
  public void addRelatedEntity(UUID entityPartUuid, UUID entityUuid) {
    Integer sortIndex =
        selectNextSortIndexForParentChildren(
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
        selectNextSortIndexForParentChildren(
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
  public P findOne(Identifier identifier) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public LinkedHashSet<E> getRelatedEntities(P entityPart) {
    return getRelatedEntities(entityPart.getUuid());
  }

  @Override
  public LinkedHashSet<E> getRelatedEntities(UUID entityPartUuid) {
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
    // TODO maybe does not work, then we have to refactor to LinkedHashSet<Entity>...
    LinkedHashSet<E> result =
        list.stream().map(s -> (E) s).collect(Collectors.toCollection(LinkedHashSet::new));
    return result;
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(P entityPart) {
    return getRelatedFileResources(entityPart.getUuid());
  }

  @Override
  public LinkedHashSet<FileResource> getRelatedFileResources(UUID entityPartUuid) {
    String query =
        "SELECT * FROM fileresources f"
            + " INNER JOIN rel_entitypart_fileresources ref ON f.uuid=ref.fileresource_uuid"
            + " WHERE ref.entitypart_uuid = :entityPartUuid"
            + " ORDER BY ref.sortindex";

    List<FileResourceImpl> result =
        dbi.withHandle(
            h ->
                h.createQuery(query)
                    .bind("entityPartUuid", entityPartUuid)
                    .mapToBean(FileResourceImpl.class)
                    .list());
    return new LinkedHashSet<>(result);
  }

  @Override
  public LinkedHashSet<E> saveRelatedEntities(P entityPart, LinkedHashSet<E> entities) {
    return saveRelatedEntities(entityPart.getUuid(), entities);
  }

  @Override
  public LinkedHashSet<E> saveRelatedEntities(UUID entityPartUuid, LinkedHashSet<E> entities) {
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
  public LinkedHashSet<FileResource> saveRelatedFileResources(
      P entityPart, LinkedHashSet<FileResource> fileResources) {
    return saveRelatedFileResources(entityPart.getUuid(), fileResources);
  }

  @Override
  public LinkedHashSet<FileResource> saveRelatedFileResources(
      UUID entityPartUuid, LinkedHashSet<FileResource> fileResources) {
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
