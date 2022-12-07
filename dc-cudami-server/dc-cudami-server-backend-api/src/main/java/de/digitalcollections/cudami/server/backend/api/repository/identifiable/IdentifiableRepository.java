package de.digitalcollections.cudami.server.backend.api.repository.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface IdentifiableRepository<I extends Identifiable> {

  static String[] splitToArray(String term) {
    term = term.toLowerCase();
    /*
     * Remove all characters that are NOT:
     * - space
     * - letter or digit
     * - underscore
     * - hyphen
     * and remove all standalone hyphens (space hyphen space)
     * (flag `U` stands for Unicode)
     */
    term = term.replaceAll("(?iU)[^\\s\\w_-]|(?<=\\s)-(?=\\s)", "");
    // Look for words with hyphens to split them too
    Matcher hyphenWords = Pattern.compile("(?iU)\\b\\w+(-\\w+)+\\b").matcher(term);
    List<String> result =
        hyphenWords
            .results()
            .collect(
                ArrayList<String>::new,
                (list, match) -> list.addAll(Arrays.asList(match.group().split("-+"))),
                ArrayList::addAll);
    for (String word : term.trim().split("\\s+")) {
      result.add(word);
    }
    return result.toArray(new String[result.size()]);
  }

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

  default List<I> find(String searchTerm, int maxResults) {
    PageRequest request = new PageRequest(searchTerm, 0, maxResults, null);
    PageResponse<I> response = find(request);
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

  default void save(I identifiable) throws RepositoryException {
    save(identifiable, null);
  }

  void save(I identifiable, Map<String, Object> bindings) throws RepositoryException;

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

  default void update(I identifiable) throws RepositoryException {
    update(identifiable, null);
  }

  void update(I identifiable, Map<String, Object> bindings) throws RepositoryException;
}
