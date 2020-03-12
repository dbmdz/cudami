package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts;

import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

public interface EntityPartService<P extends EntityPart, E extends Entity> {

  void addRelatedEntity(P entityPart, E entity);

  void addRelatedEntity(UUID entityPartUuid, UUID entityUuid);

  List<E> getRelatedEntities(P entityPart);

  List<E> getRelatedEntities(UUID entityPartUuid);

  /**
   * Save list of entities related to an entity part.Prerequisite: entities have been saved before
   * (exist already)
   *
   * @param entityPart entity part the entities are related to
   * @param entities the entities that are related to the entity part
   * @return the list of the related entities
   */
  List<E> saveRelatedEntities(P entityPart, List<E> entities);

  List<E> saveRelatedEntities(UUID entityPartUuid, List<E> entities);

  void addRelatedFileresource(P entityPart, FileResource fileResource);

  void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid);

  List<FileResource> getRelatedFileResources(P entityPart);

  List<FileResource> getRelatedFileResources(UUID entityPartUuid);

  /**
   * Save list of file resources related to an entity. Prerequisite: file resources have been saved
   * before (exist already)
   *
   * @param entityPart entity part the file resources are related to
   * @param fileResources the file resources that are related to the entity part
   * @return the list of the related file resources
   */
  List<FileResource> saveRelatedFileResources(P entityPart, List<FileResource> fileResources);

  List<FileResource> saveRelatedFileResources(
      UUID entityPartUuid, List<FileResource> fileResources);
}
