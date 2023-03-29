package de.digitalcollections.cudami.server.backend.api.repository.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
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

  void addRelatedEntity(UUID headwordUuid, UUID entityUuid) throws RepositoryException;

  void addRelatedFileresource(UUID headwordUuid, UUID fileResourceUuid) throws RepositoryException;

  /**
   * Delete a headword.
   *
   * @param label label of headword
   * @param locale locale of label
   */
  void deleteByLabelAndLocale(String label, Locale locale) throws RepositoryException;

  void deleteRelatedEntities(UUID headwordUuid) throws RepositoryException;

  void deleteRelatedFileresources(UUID headwordUuid) throws RepositoryException;

  BucketObjectsResponse<Headword> find(BucketObjectsRequest<Headword> bucketObjectsRequest)
      throws RepositoryException;

  BucketsResponse<Headword> find(BucketsRequest<Headword> bucketsRequest)
      throws RepositoryException;

  // FIXME: replace by pagerequest with filtering
  List<Headword> find(String label, Locale locale) throws RepositoryException;

  // FIXME: replace by pagerequest with filtering
  List<Headword> findByLabel(String label) throws RepositoryException;

  // TODO: replace with filtering
  PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws RepositoryException;

  PageResponse<Entity> findRelatedEntities(UUID headwordUuid, PageRequest pageRequest)
      throws RepositoryException;

  PageResponse<FileResource> findRelatedFileResources(UUID headwordUuid, PageRequest pageRequest)
      throws RepositoryException;

  /**
   * Returns a headword, if available
   *
   * @param label label of headword, e.g. "München"
   * @param locale locale of label, e.g. "de"
   * @return Headword or null
   */
  Headword getByLabelAndLocale(String label, Locale locale) throws RepositoryException;

  List<Locale> getLanguages() throws RepositoryException;

  List<Entity> getRelatedEntities(UUID headwordUuid) throws RepositoryException;

  List<FileResource> getRelatedFileResources(UUID headwordUuid) throws RepositoryException;

  List<Entity> setRelatedEntities(UUID headwordUuid, List<Entity> entities)
      throws RepositoryException;

  List<FileResource> setRelatedFileResources(UUID headwordUuid, List<FileResource> fileResources)
      throws RepositoryException;
}
