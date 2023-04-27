package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
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

  default void addRelatedEntity(I identifiable, Entity entity) throws RepositoryException {
    if (identifiable == null || entity == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    addRelatedEntity(identifiable.getUuid(), entity.getUuid());
  }

  void addRelatedEntity(UUID identifiableUuid, UUID entityUuid) throws RepositoryException;

  default void addRelatedFileresource(I identifiable, FileResource fileResource)
      throws RepositoryException {
    if (identifiable == null || fileResource == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    addRelatedFileresource(identifiable.getUuid(), fileResource.getUuid());
  }

  void addRelatedFileresource(UUID identifiableUuid, UUID fileResourceUuid)
      throws RepositoryException;

  // TODO: replace with filtering
  PageResponse<I> findByLanguageAndInitial(PageRequest pageRequest, String language, String initial)
      throws RepositoryException;

  default PageResponse<Entity> findRelatedEntities(I identifiable, PageRequest pageRequest)
      throws RepositoryException {
    if (identifiable == null) {
      throw new IllegalArgumentException("find failed: given identifiable must not be null");
    }
    return findRelatedEntities(identifiable.getUuid(), pageRequest);
  }

  PageResponse<Entity> findRelatedEntities(UUID identifiableUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<FileResource> findRelatedFileResources(
      I identifiable, PageRequest pageRequest) throws RepositoryException {
    if (identifiable == null) {
      throw new IllegalArgumentException("find failed: given identifiable must not be null");
    }
    return findRelatedFileResources(identifiable.getUuid(), pageRequest);
  }

  PageResponse<FileResource> findRelatedFileResources(
      UUID identifiableUuid, PageRequest pageRequest) throws RepositoryException;

  default I getByIdentifiable(I identifiable) throws RepositoryException {
    if (identifiable == null) {
      throw new IllegalArgumentException("get failed: given identifiable must not be null");
    }
    return getByUuid(identifiable.getUuid());
  }

  default I getByIdentifier(Identifier identifier) throws RepositoryException {
    if (identifier == null) {
      throw new IllegalArgumentException("get failed: given identifier must not be null");
    }
    return getByIdentifier(identifier.getNamespace(), identifier.getId());
  }

  I getByIdentifier(String namespace, String id) throws RepositoryException;

  List<Locale> getLanguages() throws RepositoryException;

  /**
   * Save list of entities related to an identifiable.Prerequisite: entities have been saved before
   * (exist already)
   *
   * @param identifiable identifiable the entities are related to
   * @param entities the entities that are related to the identifiable
   * @return the list of the related entities
   */
  default List<Entity> setRelatedEntities(I identifiable, List<Entity> entities)
      throws RepositoryException {
    if (identifiable == null || entities == null) {
      throw new IllegalArgumentException("set failed: given objects must not be null");
    }
    return setRelatedEntities(identifiable.getUuid(), entities);
  }

  List<Entity> setRelatedEntities(UUID identifiableUuid, List<Entity> entities)
      throws RepositoryException;

  /**
   * Save list of file resources related to an entity. Prerequisite: file resources have been saved
   * before (exist already)
   *
   * @param identifiable identifiable the file resources are related to
   * @param fileResources the file resources that are related to the identifiable
   * @return the list of the related file resources
   */
  default List<FileResource> setRelatedFileResources(
      I identifiable, List<FileResource> fileResources) throws RepositoryException {
    if (identifiable == null || fileResources == null) {
      throw new IllegalArgumentException("set failed: given objects must not be null");
    }
    return setRelatedFileResources(identifiable.getUuid(), fileResources);
  }

  List<FileResource> setRelatedFileResources(
      UUID identifiableUuid, List<FileResource> fileResources) throws RepositoryException;
}
