package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface IdentifiableService<I extends Identifiable> {

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

  default void cleanupLabelFromUnwantedLocales(
      Locale locale, Locale fallbackLocale, LocalizedText label) {
    // If no locales exist at all, we cannot do anything useful here
    if (label == null || label.getLocales() == null || label.getLocales().isEmpty()) {
      return;
    }

    // Prepare the fallback solutions, when no label for the desired locale exists.
    // Retrieve the value for the fallback locale and bypass a "feature" of the
    // LocalizedText class, which would return the "first" value, if no value for the
    // given locale exists. This is NOT what we want here!
    String defaultLabel = null;
    if (label.getLocales().contains(fallbackLocale)) {
      defaultLabel = label.getText(fallbackLocale);
    }

    Locale firstLocale = label.getLocales().get(0);
    String firstLocaleLabel = label.getText(firstLocale);

    // Remove all locale/text pairs, which don't apply to the demanded language
    // but ensure, that in the end, if nothing is left, one of the fallbacks are applied.
    label.entrySet().removeIf(e -> e.getKey() != locale);
    if (label.keySet().isEmpty()) {
      // No entry for the desired language found!
      if (defaultLabel != null) {
        // The entry for the "default" language exists. We use it.
        label.put(fallbackLocale, defaultLabel);
      } else if (firstLocale != null) {
        // Pick the first locale and its text (if it exists)
        label.put(firstLocale, firstLocaleLabel);
      }
    }
  }

  long count();

  default boolean delete(UUID uuid) throws IdentifiableServiceException {
    return delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  boolean delete(List<UUID> uuids) throws IdentifiableServiceException;

  PageResponse<I> find(PageRequest pageRequest);

  SearchPageResponse<I> find(SearchPageRequest searchPageRequest);

  List<I> find(String searchTerm, int maxResults);

  /**
   * @return list of ALL identifiables with FULL data. USE WITH CARE (only for internal workflow,
   *     NOT FOR USER INTERACTION!)!!!
   */
  List<I> findAllFull();

  /**
   * Returns a list of all identifiables, reduced to their identifiers and last modification date
   *
   * @return partially filled complete list of all identifiables of implementing repository entity
   *     type
   */
  List<I> findAllReduced();

  PageResponse<I> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  I get(Identifier identifier);

  I get(UUID uuid);

  I get(UUID uuid, Locale locale) throws IdentifiableServiceException;

  I getByIdentifier(String namespace, String id);

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

  I save(I identifiable) throws IdentifiableServiceException;

  /**
   * Save list of entities related to an identifiable.Prerequisite: entities have been saved before
   * (exist already)
   *
   * @param identifiable entity part the entities are related to
   * @param entities the entities that are related to the entity part
   * @return the list of the related entities
   */
  default List<Entity> saveRelatedEntities(I identifiable, List<Entity> entities) {
    if (identifiable == null || entities == null) {
      return null;
    }
    return saveRelatedEntities(identifiable.getUuid(), entities);
  }

  List<Entity> saveRelatedEntities(UUID identifiableUuid, List<Entity> entities);

  /**
   * Save list of file resources related to an identifiable. Prerequisite: file resources have been
   * saved before (exist already)
   *
   * @param identifiable entity part the file resources are related to
   * @param fileResources the file resources that are related to the entity part
   * @return the list of the related file resources
   */
  default List<FileResource> saveRelatedFileResources(
      I identifiable, List<FileResource> fileResources) {
    if (identifiable == null || fileResources == null) {
      return null;
    }
    return saveRelatedFileResources(identifiable.getUuid(), fileResources);
  }

  List<FileResource> saveRelatedFileResources(
      UUID identifiableUuid, List<FileResource> fileResources);

  I update(I identifiable) throws IdentifiableServiceException;
}
