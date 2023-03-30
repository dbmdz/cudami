package de.digitalcollections.cudami.server.business.impl.service.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.semantic.HeadwordRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.semantic.HeadwordService;
import de.digitalcollections.model.identifiable.entity.Entity;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.list.buckets.BucketObjectsRequest;
import de.digitalcollections.model.list.buckets.BucketObjectsResponse;
import de.digitalcollections.model.list.buckets.BucketsRequest;
import de.digitalcollections.model.list.buckets.BucketsResponse;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Headword;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HeadwordServiceImpl implements HeadwordService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordServiceImpl.class);

  private final HeadwordRepository repository;

  public HeadwordServiceImpl(HeadwordRepository repository) {
    this.repository = repository;
  }

  @Override
  public void addRelatedEntity(UUID headwordUuid, UUID entityUuid) {
    repository.addRelatedEntity(headwordUuid, entityUuid);
  }

  @Override
  public void addRelatedFileresource(UUID headwordUuid, UUID fileResourceUuid) {
    repository.addRelatedFileresource(headwordUuid, fileResourceUuid);
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public boolean delete(List<UUID> uuids) {
    return repository.deleteByUuid(uuids);
  }

  @Override
  public PageResponse<Headword> find(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    return repository.find(pageRequest);
  }

  @Override
  public List<Headword> find(String searchTerm, int maxResults) {
    return repository.getByExampleAndMimetype(searchTerm, maxResults);
  }

  @Override
  public BucketsResponse<Headword> find(BucketsRequest<Headword> bucketsRequest) {
    return repository.find(bucketsRequest);
  }

  @Override
  public BucketObjectsResponse<Headword> find(BucketObjectsRequest<Headword> bucketObjectsRequest) {
    return repository.find(bucketObjectsRequest);
  }

  @Override
  public List<Headword> getByLabelAndLocale(String label, Locale locale) {
    return repository.find(label, locale);
  }

  @Override
  public PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) {
    PageResponse<Headword> result =
        repository.findByLanguageAndInitial(pageRequest, language, initial);
    return result;
  }

  @Override
  public PageResponse<Entity> findRelatedEntities(UUID headwordUuid, PageRequest pageRequest) {
    return repository.findRelatedEntities(headwordUuid, pageRequest);
  }

  @Override
  public PageResponse<FileResource> findRelatedFileResources(
      UUID headwordUuid, PageRequest pageRequest) {
    return repository.findRelatedFileResources(headwordUuid, pageRequest);
  }

  @Override
  public List<Headword> getAll() {
    return repository.getAll();
  }

  @Override
  public Headword getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public List<Locale> getLanguages() {
    return repository.getLanguages();
  }

  @Override
  public List<Headword> getRandom(int count) {
    return repository.getRandom(count);
  }

  @Override
  public List<Entity> getRelatedEntities(UUID headwordUuid) {
    return repository.getRelatedEntities(headwordUuid);
  }

  @Override
  public List<FileResource> getRelatedFileResources(UUID headwordUuid) {
    return repository.getRelatedFileResources(headwordUuid);
  }

  @Override
  public Headword save(Headword headword) throws ServiceException {
    try {
      return repository.save(headword);
    } catch (Exception e) {
      LOGGER.error("Cannot save headword " + headword.getLabel() + ": ", e);
      throw new ServiceException(e.getMessage());
    }
  }

  protected void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "label", "uuid");
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public List<Entity> setRelatedEntities(UUID headwordUuid, List<Entity> entities) {
    return repository.setRelatedEntities(headwordUuid, entities);
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      UUID headwordUuid, List<FileResource> fileResources) {
    return repository.setRelatedFileResources(headwordUuid, fileResources);
  }

  @Override
  public Headword update(Headword headword) throws ServiceException {
    try {
      return repository.update(headword);
    } catch (Exception e) {
      LOGGER.error("Cannot update headword " + headword + ": ", e);
      throw new ServiceException(e.getMessage());
    }
  }
}
