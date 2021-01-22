package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.entity.Entity;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;

/** @param <E> entity instance */
public interface EntityService<E extends Entity> extends IdentifiableService<E> {

  default void addRelatedFileresource(E entity, FileResource fileResource) {
    addRelatedFileresource(entity.getUuid(), fileResource.getUuid());
  }

  void addRelatedFileresource(UUID entityUuid, UUID fileResourceUuid);

  E getByRefId(long refId);

  default List<FileResource> getRelatedFileResources(E entity) {
    return getRelatedFileResources(entity.getUuid());
  }

  List<FileResource> getRelatedFileResources(UUID entityUuid);

  /**
   * Save list of file resources related to an entity. Prerequisite: file resources have been saved
   * before (exist already)
   *
   * @param entity entity the fileresources are related to
   * @param fileResources the fileresources that are related to the entity
   * @return the list of the related fileresources
   */
  default List<FileResource> saveRelatedFileResources(E entity, List<FileResource> fileResources) {
    return saveRelatedFileResources(entity.getUuid(), fileResources);
  }

  List<FileResource> saveRelatedFileResources(UUID entityUuid, List<FileResource> fileResources);
}
