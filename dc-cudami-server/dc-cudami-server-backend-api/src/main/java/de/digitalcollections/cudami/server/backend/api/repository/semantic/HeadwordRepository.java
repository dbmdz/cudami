package de.digitalcollections.cudami.server.backend.api.repository.semantic;

import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
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

  BucketsResponse<Headword> find(BucketsRequest<Headword> bucketsRequest);

  /**
   * Return paged list of headwords
   *
   * @param pageRequest request for page
   * @return page response
   */
  PageResponse<Headword> find(PageRequest pageRequest);

  default List<Headword> find(String searchTerm, int maxResults) {
    PageRequest request = new PageRequest(searchTerm, 0, maxResults, null);
    PageResponse<Headword> response = find(request);
    return response.getContent();
  }

  List<Headword> find(String label, Locale locale);

  /**
   * Return all headwords
   *
   * @return List of all headwords
   */
  List<Headword> getAll();

  /**
   * Returns a list of headwords, if available
   *
   * @param label label of headword, e.g. "München" (locale ignored)
   * @return list of headwords or null
   */
  List<Headword> findByLabel(String label);

  PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  List<Headword> getRandom(int count);
  /**
   * Returns a headword, if available
   *
   * @param label label of headword, e.g. "München"
   * @param locale locale of label, e.g. "de"
   * @return Headword or null
   */
  Headword getByLabelAndLocale(String label, Locale locale);

  default Headword getByUuid(UUID uuid) {
    return findByUuidAndFiltering(uuid, null);
  }

  Headword findByUuidAndFiltering(UUID uuid, Filtering filtering);

  List<Locale> getLanguages();

  List<Entity> getRelatedEntities(UUID headwordUuid);

  PageResponse<Entity> findRelatedEntities(UUID headwordUuid, PageRequest pageRequest);

  List<FileResource> getRelatedFileResources(UUID headwordUuid);

  PageResponse<FileResource> findRelatedFileResources(UUID headwordUuid, PageRequest pageRequest);

  /**
   * Save a Headword.
   *
   * @param headword the headword to be saved
   * @return the saved headword with updated timestamps
   */
  Headword save(Headword headword);

  List<Entity> setRelatedEntities(UUID headwordUuid, List<Entity> entities);

  List<FileResource> setRelatedFileResources(UUID headwordUuid, List<FileResource> fileResources);

  Headword update(Headword headword);
}
