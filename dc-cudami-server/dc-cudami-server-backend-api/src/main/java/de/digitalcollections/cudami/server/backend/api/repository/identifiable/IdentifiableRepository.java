package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.model.filter.Filtering;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public interface IdentifiableRepository<I extends Identifiable> {

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

  long count();

  default void delete(UUID uuid) {
    delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  boolean delete(List<UUID> uuids);

  PageResponse<I> find(PageRequest pageRequest);

  SearchPageResponse<I> find(SearchPageRequest searchPageRequest);

  default List<I> find(String searchTerm, int maxResults) {
    SearchPageRequest request = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<I> response = find(request);
    return response.getContent();
  }

  /**
   * @return list of ALL identifiables with FULL data. USE WITH CARE (only for internal workflow,
   *     NOT FOR USER INTERACTION!)!!!
   */
  List<I> getAllFull();

  /**
   * Returns a list of all identifiables, reduced to their identifiers and last modification date
   *
   * @return partially filled complete list of all identifiables of implementing repository entity
   *     type
   */
  List<I> getAllReduced();

  PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  I getByIdentifier(Identifier identifier);

  default I getByUuid(UUID uuid) {
    return getByUuidAndFiltering(uuid, null);
  }

  I getByUuidAndFiltering(UUID uuid, Filtering filtering);

  default I getByIdentifier(String namespace, String id) {
    return getByIdentifier(new Identifier(null, namespace, id));
  }

  List<Locale> getLanguages();

  default List<Entity> getRelatedEntities(I identifiable) {
    if (identifiable == null) {
      return null;
    }
    return getRelatedEntities(identifiable.getUuid());
  }

  List<Entity> getRelatedEntities(UUID identifiableUuid);

  default List<FileResource> getRelatedFileResources(I identifiable) {
    if (identifiable == null) {
      return null;
    }
    return getRelatedFileResources(identifiable.getUuid());
  }

  List<FileResource> getRelatedFileResources(UUID identifiableUuid);

  default I save(I identifiable) {
    return save(identifiable, null);
  }

  I save(I identifiable, Map<String, Object> bindings);

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

  default I update(I identifiable) {
    return update(identifiable, null);
  }

  I update(I identifiable, Map<String, Object> bindings);
}
