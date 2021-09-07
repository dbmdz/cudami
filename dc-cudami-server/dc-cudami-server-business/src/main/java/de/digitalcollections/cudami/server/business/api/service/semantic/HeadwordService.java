package de.digitalcollections.cudami.server.business.api.service.semantic;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.semantic.Headword;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Service for Headword. */
public interface HeadwordService {

  default void addRelatedEntity(Headword headword, Entity entity) {
    if (headword == null || entity == null) {
      return;
    }
    addRelatedEntity(headword.getUuid(), entity.getUuid());
  }

  void addRelatedEntity(UUID headwordUuid, UUID entityUuid);

  default void addRelatedFileresource(Headword headword, FileResource fileResource) {
    if (headword == null || fileResource == null) {
      return;
    }
    addRelatedFileresource(headword.getUuid(), fileResource.getUuid());
  }

  void addRelatedFileresource(UUID headwordUuid, UUID fileResourceUuid);

  long count();

  default boolean delete(UUID uuid) {
    return delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  boolean delete(List<UUID> uuids);

  PageResponse<Headword> find(PageRequest pageRequest);

  SearchPageResponse<Headword> find(SearchPageRequest searchPageRequest);

  List<Headword> find(String searchTerm, int maxResults);

  /**
   * @return list of ALL headwords. USE WITH CARE (only for internal workflow, NOT FOR USER
   *     INTERACTION!)!!!
   */
  List<Headword> findAll();

  PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  Headword get(UUID uuid);

  List<Headword> get(String label, Locale locale);

  List<Locale> getLanguages();

  List<Headword> getRandom(int count);

  default List<Entity> getRelatedEntities(Headword headword) {
    if (headword == null) {
      return null;
    }
    return getRelatedEntities(headword.getUuid());
  }

  List<Entity> getRelatedEntities(UUID headwordUuid);

  default List<FileResource> getRelatedFileResources(Headword headword) {
    if (headword == null) {
      return null;
    }
    return getRelatedFileResources(headword.getUuid());
  }

  List<FileResource> getRelatedFileResources(UUID headwordUuid);

  Headword save(Headword headword) throws ServiceException;

  /**
   * Save list of entities related to an Headword. Prerequisite: entities have been saved before
   * (exist already)
   *
   * @param headword headword the entities are related to
   * @param entities the entities that are related to the headword
   * @return the list of the related entities
   */
  default List<Entity> saveRelatedEntities(Headword headword, List<Entity> entities) {
    if (headword == null || entities == null) {
      return null;
    }
    return saveRelatedEntities(headword.getUuid(), entities);
  }

  List<Entity> saveRelatedEntities(UUID headwordUuid, List<Entity> entities);

  /**
   * Save list of file resources related to an Headword. Prerequisite: file resources have been
   * saved before (exist already)
   *
   * @param headword headword the file resources are related to
   * @param fileResources the file resources that are related to the entity part
   * @return the list of the related file resources
   */
  default List<FileResource> saveRelatedFileResources(
      Headword headword, List<FileResource> fileResources) {
    if (headword == null || fileResources == null) {
      return null;
    }
    return saveRelatedFileResources(headword.getUuid(), fileResources);
  }

  List<FileResource> saveRelatedFileResources(UUID headwordUuid, List<FileResource> fileResources);

  Headword update(Headword headword) throws ServiceException;
}
