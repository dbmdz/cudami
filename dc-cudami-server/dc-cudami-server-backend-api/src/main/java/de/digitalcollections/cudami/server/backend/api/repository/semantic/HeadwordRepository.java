package de.digitalcollections.cudami.server.backend.api.repository.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.buckets.BucketObjectsRequest;
import de.digitalcollections.model.list.buckets.BucketObjectsResponse;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Headword;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Repository for Headwords handling */
public interface HeadwordRepository extends UniqueObjectRepository<Headword> {

  void addRelatedEntity(UUID headwordUuid, UUID entityUuid);

  void addRelatedFileresource(UUID headwordUuid, UUID fileResourceUuid);

  /**
   * Delete a headword.
   *
   * @param label label of headword
   * @param locale locale of label
   */
  void deleteByLabelAndLocale(String label, Locale locale);

  void deleteRelatedEntities(UUID headwordUuid);

  void deleteRelatedFileresources(UUID headwordUuid);

  BucketObjectsResponse<Headword> find(BucketObjectsRequest<Headword> bucketObjectsRequest);

  BucketsResponse<Headword> find(BucketsRequest<Headword> bucketsRequest);

  // TODO: replace with filtering
  PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial);

  PageResponse<Entity> findRelatedEntities(UUID headwordUuid, PageRequest pageRequest);

  PageResponse<FileResource> findRelatedFileResources(UUID headwordUuid, PageRequest pageRequest);

  /**
   * Returns a headword, if available
   *
   * @param label label of headword, e.g. "München"
   * @param locale locale of label, e.g. "de"
   * @return Headword or null
   */
  Headword getByLabelAndLocale(String label, Locale locale);

  List<Locale> getLanguages();

  List<Headword> getRandom(int count);

  List<Entity> getRelatedEntities(UUID headwordUuid);

  List<FileResource> getRelatedFileResources(UUID headwordUuid);

  List<Entity> setRelatedEntities(UUID headwordUuid, List<Entity> entities);

  List<FileResource> setRelatedFileResources(UUID headwordUuid, List<FileResource> fileResources);
}
