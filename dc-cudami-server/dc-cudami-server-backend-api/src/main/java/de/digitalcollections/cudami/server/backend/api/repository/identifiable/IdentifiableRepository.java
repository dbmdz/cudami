package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface IdentifiableRepository<I extends Identifiable> extends UniqueObjectRepository<I> {

  default void addRelatedEntity(I identifiable, Entity entity) {
    if (identifiable == null || entity == null) {
      return;
    }
    addRelatedEntity(identifiable.getUuid(), entity.getUuid());
  }

  void addRelatedEntity(UUID identifiableUuid, UUID entityUuid);

  default void addRelatedFileresource(I identifiable, FileResource fileResource) {
    if (identifiable == null || fileResource == null) {
      return;
    }
    addRelatedFileresource(identifiable.getUuid(), fileResource.getUuid());
  }

  void addRelatedFileresource(UUID identifiableUuid, UUID fileResourceUuid);

  PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  default PageResponse<Entity> findRelatedEntities(I identifiable, PageRequest pageRequest) {
    if (identifiable == null) {
      return null;
    }
    return findRelatedEntities(identifiable.getUuid(), pageRequest);
  }

  PageResponse<Entity> findRelatedEntities(UUID identifiableUuid, PageRequest pageRequest);

  default PageResponse<FileResource> findRelatedFileResources(
      I identifiable, PageRequest pageRequest) {
    if (identifiable == null) {
      return null;
    }
    return findRelatedFileResources(identifiable.getUuid(), pageRequest);
  }

  PageResponse<FileResource> findRelatedFileResources(
      UUID identifiableUuid, PageRequest pageRequest);

  default I getByIdentifiable(I identifiable) {
    if (identifiable == null) {
      return null;
    }
    return getByUuid(identifiable.getUuid());
  }

  default I getByIdentifier(Identifier identifier) {
    if (identifier == null) {
      return null;
    }
    if (identifier.getIdentifiable() != null) {
      return getByUuid(identifier.getIdentifiable());
    }
    return getByIdentifier(identifier.getNamespace(), identifier.getId());
  }

  I getByIdentifier(String namespace, String id);

  List<Locale> getLanguages();

  List<I> getRandom(int count);

  /**
   * Save list of entities related to an identifiable.Prerequisite: entities have been saved before
   * (exist already)
   *
   * @param identifiable identifiable the entities are related to
   * @param entities the entities that are related to the identifiable
   * @return the list of the related entities
   */
  default List<Entity> setRelatedEntities(I identifiable, List<Entity> entities) {
    if (identifiable == null || entities == null) {
      return null;
    }
    return setRelatedEntities(identifiable.getUuid(), entities);
  }

  List<Entity> setRelatedEntities(UUID identifiableUuid, List<Entity> entities);

  /**
   * Save list of file resources related to an entity. Prerequisite: file resources have been saved
   * before (exist already)
   *
   * @param identifiable identifiable the file resources are related to
   * @param fileResources the file resources that are related to the identifiable
   * @return the list of the related file resources
   */
  default List<FileResource> setRelatedFileResources(
      I identifiable, List<FileResource> fileResources) {
    if (identifiable == null || fileResources == null) {
      return null;
    }
    return setRelatedFileResources(identifiable.getUuid(), fileResources);
  }

  List<FileResource> setRelatedFileResources(
      UUID identifiableUuid, List<FileResource> fileResources);
}
