package de.digitalcollections.cudami.admin.backend.api.repository.identifiable.entity.parts;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.entity.parts.EntityPart;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.UUID;

public interface EntityPartRepository<P extends EntityPart, E extends Entity>
    extends IdentifiableRepository<P> {

  void addRelatedEntity(P entityPart, E entity);

  void addRelatedEntity(UUID entityPartUuid, UUID entityUuid);

  LinkedHashSet<E> getRelatedEntities(P entityPart);

  LinkedHashSet<E> getRelatedEntities(UUID entityPartUuid);

  /**
   * Save list of entities related to an entity part.Prerequisite: entities have been saved before
   * (exist already)
   *
   * @param entityPart entity part the entities are related to
   * @param entities the entities that are related to the entity part
   * @return the list of the related entities
   */
  LinkedHashSet<E> saveRelatedEntities(P entityPart, LinkedHashSet<E> entities);

  LinkedHashSet<E> saveRelatedEntities(UUID entityPartUuid, LinkedHashSet<E> entities);

  void addRelatedFileresource(P entityPart, FileResource fileResource);

  void addRelatedFileresource(UUID entityPartUuid, UUID fileResourceUuid);

  LinkedHashSet<FileResource> getRelatedFileResources(P entityPart);

  LinkedHashSet<FileResource> getRelatedFileResources(UUID entityPartUuid);

  /**
   * Save list of file resources related to an entity. Prerequisite: file resources have been saved
   * before (exist already)
   *
   * @param entityPart entity part the file resources are related to
   * @param fileResources the file resources that are related to the entity part
   * @return the list of the related file resources
   */
  LinkedHashSet<FileResource> saveRelatedFileResources(
      P entityPart, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveRelatedFileResources(
      UUID entityPartUuid, LinkedHashSet<FileResource> fileResources);
}
