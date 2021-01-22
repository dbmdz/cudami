package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

public interface EntityPartService<P extends EntityPart> extends IdentifiableService<P> {

  default void addRelatedEntity(P entityPart, Entity entity) {
    if (entityPart == null || entity == null) {
      return;
    }
    addRelatedEntity(entityPart.getUuid(), entity.getUuid());
  }

  void addRelatedEntity(UUID entityPartUuid, UUID entityUuid);

  default void addRelatedFileresource(P entityPart, FileResource fileResource) {
    if (entityPart == null || fileResource == null) {
      return;
    }
    addRelatedFileresource(entityPart.getUuid(), fileResource.getUuid());
  }

  void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid);

  default List<Entity> getRelatedEntities(P entityPart) {
    if (entityPart == null) {
      return null;
    }
    return getRelatedEntities(entityPart.getUuid());
  }

  List<Entity> getRelatedEntities(UUID entityPartUuid);

  default List<FileResource> getRelatedFileResources(P entityPart) {
    if (entityPart == null) {
      return null;
    }
    return getRelatedFileResources(entityPart.getUuid());
  }

  List<FileResource> getRelatedFileResources(UUID entityPartUuid);

  /**
   * Save list of entities related to an entity part.Prerequisite: entities have been saved before
   * (exist already)
   *
   * @param entityPart entity part the entities are related to
   * @param entities the entities that are related to the entity part
   * @return the list of the related entities
   */
  default List<Entity> saveRelatedEntities(P entityPart, List<Entity> entities) {
    if (entityPart == null || entities == null) {
      return null;
    }
    return saveRelatedEntities(entityPart.getUuid(), entities);
  }

  List<Entity> saveRelatedEntities(UUID entityPartUuid, List<Entity> entities);

  /**
   * Save list of file resources related to an entity. Prerequisite: file resources have been saved
   * before (exist already)
   *
   * @param entityPart entity part the file resources are related to
   * @param fileResources the file resources that are related to the entity part
   * @return the list of the related file resources
   */
  default List<FileResource> saveRelatedFileResources(
      P entityPart, List<FileResource> fileResources) {
    if (entityPart == null || fileResources == null) {
      return null;
    }
    return saveRelatedFileResources(entityPart.getUuid(), fileResources);
  }

  List<FileResource> saveRelatedFileResources(
      UUID entityPartUuid, List<FileResource> fileResources);
}
