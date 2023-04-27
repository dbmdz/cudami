package de.digitalcollections.cudami.server.business.impl.service.semantic;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.semantic.HeadwordRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.semantic.HeadwordService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HeadwordServiceImpl extends UniqueObjectServiceImpl<Headword, HeadwordRepository>
    implements HeadwordService {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeadwordServiceImpl.class);

  public HeadwordServiceImpl(HeadwordRepository repository) {
    super(repository);
  }

  @Override
  public void addRelatedEntity(Headword headword, Entity entity) throws ServiceException {
    try {
      repository.addRelatedEntity(headword, entity);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void addRelatedFileresource(Headword headword, FileResource fileResource)
      throws ServiceException {
    try {
      repository.addRelatedFileresource(headword, fileResource);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public BucketObjectsResponse<Headword> find(BucketObjectsRequest<Headword> bucketObjectsRequest)
      throws ServiceException {
    try {
      return repository.find(bucketObjectsRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public BucketsResponse<Headword> find(BucketsRequest<Headword> bucketsRequest)
      throws ServiceException {
    try {
      return repository.find(bucketsRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<Headword> findByLanguageAndInitial(
      PageRequest pageRequest, String language, String initial) throws ServiceException {
    PageResponse<Headword> result;
    try {
      result = repository.findByLanguageAndInitial(pageRequest, language, initial);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return result;
  }

  @Override
  public PageResponse<Entity> findRelatedEntities(Headword headword, PageRequest pageRequest)
      throws ServiceException {
    try {
      return repository.findRelatedEntities(headword, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<FileResource> findRelatedFileResources(
      Headword headword, PageRequest pageRequest) throws ServiceException {
    try {
      return repository.findRelatedFileResources(headword, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Headword> getByLabelAndLocale(String label, Locale locale) throws ServiceException {
    try {
      return repository.find(label, locale);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Locale> getLanguages() throws ServiceException {
    try {
      return repository.getLanguages();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Entity> getRelatedEntities(Headword headword) throws ServiceException {
    try {
      return repository.getRelatedEntities(headword);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<FileResource> getRelatedFileResources(Headword headword) throws ServiceException {
    try {
      return repository.getRelatedFileResources(headword);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  protected void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "label", "uuid");
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public List<Entity> setRelatedEntities(Headword headword, List<Entity> entities)
      throws ServiceException {
    try {
      return repository.setRelatedEntities(headword, entities);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<FileResource> setRelatedFileResources(
      Headword headword, List<FileResource> fileResources) throws ServiceException {
    try {
      return repository.setRelatedFileResources(headword, fileResources);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }
}
