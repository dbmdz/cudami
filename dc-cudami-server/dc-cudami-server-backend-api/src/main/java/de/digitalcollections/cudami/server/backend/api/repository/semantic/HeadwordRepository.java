package de.digitalcollections.cudami.server.backend.api.repository.semantic;

import de.digitalcollections.model.filter.Filtering;
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

/** Repository for Headwords handling */
public interface HeadwordRepository {

  void addRelatedEntity(UUID headwordUuid, UUID entityUuid);

  void addRelatedFileresource(UUID headwordUuid, UUID fileResourceUuid);

  long count();

  /**
   * Delete a headword.
   *
   * @param label label of headword
   * @param locale locale of label
   */
  void delete(String label, Locale locale);

  void delete(UUID uuid);

  boolean delete(List<UUID> uuids);

  void deleteRelatedEntities(UUID headwordUuid);

  void deleteRelatedFileresources(UUID headwordUuid);

  /**
   * Return paged list of headwords
   *
   * @param pageRequest request for page
   * @return page response
   */
  PageResponse<Headword> find(PageRequest pageRequest);

  SearchPageResponse<Headword> find(SearchPageRequest searchPageRequest);

  default List<Headword> find(String searchTerm, int maxResults) {
    SearchPageRequest request = new SearchPageRequest(searchTerm, 0, maxResults, null);
    SearchPageResponse<Headword> response = find(request);
    return response.getContent();
  }

  List<Headword> find(String label, Locale locale);

  /**
   * Return all headwords
   *
   * @return List of all headwords
   */
  List<Headword> findAll();

  /**
   * Returns a list of headwords, if available
   *
   * @param label label of headword, e.g. "München" (locale ignored)
   * @return list of headwords or null
   */
  List<Headword> findByLabel(String label);

  PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  default Headword findOne(UUID uuid) {
    return findOne(uuid, null);
  }

  Headword findOne(UUID uuid, Filtering filtering);

  /**
   * Returns a headword, if available
   *
   * @param label label of headword, e.g. "München"
   * @param locale locale of label, e.g. "de"
   * @return Headword or null
   */
  Headword findOneByLabelAndLocale(String label, Locale locale);

  List<Headword> findRandom(int count);

  List<Locale> getLanguages();

  List<Entity> getRelatedEntities(UUID headwordUuid);

  List<FileResource> getRelatedFileResources(UUID headwordUuid);

  /**
   * Save a Headword.
   *
   * @param headword the headword to be saved
   * @return the saved headword with updated timestamps
   */
  Headword save(Headword headword);

  List<Entity> saveRelatedEntities(UUID headwordUuid, List<Entity> entities);

  List<FileResource> saveRelatedFileResources(UUID headwordUuid, List<FileResource> fileResources);

  Headword update(Headword headword);
}
