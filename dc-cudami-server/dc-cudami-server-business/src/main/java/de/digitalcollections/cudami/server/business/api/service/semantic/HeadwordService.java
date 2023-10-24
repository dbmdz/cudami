package de.digitalcollections.cudami.server.business.api.service.semantic;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
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

/** Service for Headword. */
public interface HeadwordService extends UniqueObjectService<Headword> {

  void addRelatedEntity(Headword headword, Entity entity) throws ServiceException;

  void addRelatedFileresource(Headword headword, FileResource fileResource) throws ServiceException;

  BucketObjectsResponse<Headword> find(BucketObjectsRequest<Headword> bucketObjectsRequest)
      throws ServiceException;

  BucketsResponse<Headword> find(BucketsRequest<Headword> bucketsRequest) throws ServiceException;

  PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws ServiceException;

  PageResponse<Entity> findRelatedEntities(Headword headword, PageRequest pageRequest)
      throws ServiceException;

  PageResponse<FileResource> findRelatedFileResources(Headword headword, PageRequest pageRequest)
      throws ServiceException;

  List<Headword> getByLabelAndLocale(String label, Locale locale) throws ServiceException;

  List<Locale> getLanguages() throws ServiceException;

  @Override
  List<Headword> getRandom(int count) throws ServiceException;

  List<Entity> getRelatedEntities(Headword headword) throws ServiceException;

  List<FileResource> getRelatedFileResources(Headword headword) throws ServiceException;

  /**
   * Save list of entities related to an Headword. Prerequisite: entities have been saved before
   * (exist already)
   *
   * @param headword headword the entities are related to
   * @param entities the entities that are related to the headword
   * @return the list of the related entities
   * @throws ServiceException
   */
  List<Entity> setRelatedEntities(Headword headword, List<Entity> entities) throws ServiceException;

  /**
   * Save list of file resources related to an Headword. Prerequisite: file resources have been
   * saved before (exist already)
   *
   * @param headword headword the file resources are related to
   * @param fileResources the file resources that are related to the entity part
   * @return the list of the related file resources
   * @throws ServiceException
   */
  List<FileResource> setRelatedFileResources(Headword headword, List<FileResource> fileResources)
      throws ServiceException;
}
