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

  default void addRelatedEntity(Headword headword, Entity entity) throws RepositoryException {
    if (headword == null || entity == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    addRelatedEntity(headword.getUuid(), entity.getUuid());
  }

  void addRelatedEntity(UUID headwordUuid, UUID entityUuid) throws RepositoryException;

  default void addRelatedFileresource(Headword headword, FileResource fileResource)
      throws RepositoryException {
    if (headword == null || fileResource == null) {
      throw new IllegalArgumentException("add failed: given objects must not be null");
    }
    addRelatedFileresource(headword.getUuid(), fileResource.getUuid());
  }

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

  default PageResponse<Entity> findRelatedEntities(Headword headword, PageRequest pageRequest)
      throws RepositoryException {
    if (headword == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findRelatedEntities(headword.getUuid(), pageRequest);
  }

  PageResponse<Entity> findRelatedEntities(UUID headwordUuid, PageRequest pageRequest)
      throws RepositoryException;

  default PageResponse<FileResource> findRelatedFileResources(
      Headword headword, PageRequest pageRequest) throws RepositoryException {
    if (headword == null) {
      throw new IllegalArgumentException("find failed: given object must not be null");
    }
    return findRelatedFileResources(headword.getUuid(), pageRequest);
  }

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

  default List<Entity> getRelatedEntities(Headword headword) throws RepositoryException {
    if (headword == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getRelatedEntities(headword.getUuid());
  }

  List<Entity> getRelatedEntities(UUID headwordUuid) throws RepositoryException;

  default List<FileResource> getRelatedFileResources(Headword headword) throws RepositoryException {
    if (headword == null) {
      throw new IllegalArgumentException("get failed: given object must not be null");
    }
    return getRelatedFileResources(headword.getUuid());
  }

  List<FileResource> getRelatedFileResources(UUID headwordUuid) throws RepositoryException;

  default List<Entity> setRelatedEntities(Headword headword, List<Entity> entities)
      throws RepositoryException {
    if (headword == null || entities == null) {
      throw new IllegalArgumentException("set failed: given objects must not be null");
    }
    return setRelatedEntities(headword.getUuid(), entities);
  }

  List<Entity> setRelatedEntities(UUID headwordUuid, List<Entity> entities)
      throws RepositoryException;

  default List<FileResource> setRelatedFileResources(
      Headword headword, List<FileResource> fileResources) throws RepositoryException {
    if (headword == null || fileResources == null) {
      throw new IllegalArgumentException("set failed: given objects must not be null");
    }
    return setRelatedFileResources(headword.getUuid(), fileResources);
  }

  List<FileResource> setRelatedFileResources(UUID headwordUuid, List<FileResource> fileResources)
      throws RepositoryException;
}
