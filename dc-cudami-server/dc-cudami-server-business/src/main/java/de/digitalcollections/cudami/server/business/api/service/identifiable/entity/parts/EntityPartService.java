package de.digitalcollections.cudami.server.business.api.service.identifiable.entity.parts;

import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.UUID;

public interface EntityPartService<EP extends EntityPart, E extends Entity> {

  void addRelatedEntity(EP entityPart, E entity);

  void addRelatedEntity(UUID entityPartUuid, UUID entityUuid);

  LinkedHashSet<E> getRelatedEntities(EP entityPart);

  LinkedHashSet<E> getRelatedEntities(UUID entityPartUuid);

  /**
   * Save list of entities related to an entity part.Prerequisite: entities have been saved before (exist already)
   * @param entityPart entity part the entities are related to
   * @param entities the entities that are related to the entity part
   * @return the list of the related entities
   */
  LinkedHashSet<E> saveRelatedEntities(EP entityPart, LinkedHashSet<E> entities);

  LinkedHashSet<E> saveRelatedEntities(UUID entityPartUuid, LinkedHashSet<E> entities);

  void addRelatedFileresource(EP entityPart, FileResource fileResource);

  void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid);

  LinkedHashSet<FileResource> getRelatedFileResources(EP entityPart);

  LinkedHashSet<FileResource> getRelatedFileResources(UUID entityPartUuid);

  /**
   * Save list of file resources related to an entity. Prerequisite: file resources have been saved before (exist already)
   * @param entityPart entity part the file resources are related to
   * @param fileResources the file resources that are related to the entity part
   * @return the list of the related file resources
   */
  LinkedHashSet<FileResource> saveRelatedFileResources(EP entityPart, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveRelatedFileResources(UUID entityPartUuid, LinkedHashSet<FileResource> fileResources);
}
