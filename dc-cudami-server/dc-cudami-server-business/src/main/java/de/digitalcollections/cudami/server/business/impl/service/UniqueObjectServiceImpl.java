package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.validation.ValidationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@Transactional(rollbackFor = Exception.class)
public abstract class UniqueObjectServiceImpl<
        U extends UniqueObject, R extends UniqueObjectRepository<U>>
    implements UniqueObjectService<U> {

  protected R repository;

  protected UniqueObjectServiceImpl(R repository) {
    this.repository = repository;
  }

  @Override
  public long count() throws ServiceException {
    try {
      return repository.count();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public U create() throws ServiceException {
    try {
      return repository.create();
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public int delete(Set<U> uniqueObjects) throws ConflictException, ServiceException {
    try {
      return repository.delete(uniqueObjects);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public boolean delete(U uniqueObject) throws ConflictException, ServiceException {
    try {
      return repository.delete(uniqueObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public PageResponse<U> find(PageRequest pageRequest) throws ServiceException {
    setDefaultSorting(pageRequest);
    try {
      PageResponse<U> response = repository.find(pageRequest);
      return response;
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Set<U> getAll() throws ServiceException {
    Set<U> allIdentifierTypes = new HashSet<>(1);
    PageRequest pageRequest = PageRequest.builder().pageNumber(0).pageSize(100).build();
    try {
      return getAll(allIdentifierTypes, pageRequest);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  private Set<U> getAll(Set<U> allIdentifierTypes, PageRequest pageRequest)
      throws RepositoryException {
    PageResponse<U> pageResponse = repository.find(pageRequest);
    if (pageResponse.hasContent()) {
      allIdentifierTypes.addAll(pageResponse.getContent());
    }
    if (pageResponse.hasNext()) {
      getAll(allIdentifierTypes, pageResponse.nextPageRequest());
    }
    return allIdentifierTypes;
  }

  @Override
  public List<U> getByExamples(List<U> uniqueObjects) throws ServiceException {
    try {
      return repository.getByExamples(uniqueObjects);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<U> getByExamplesAndFiltering(List<U> uniqueObjects, Filtering filtering)
      throws ServiceException {
    try {
      return repository.getByExamplesAndFiltering(uniqueObjects, filtering);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<U> getRandom(int count) throws ServiceException {
    try {
      return repository.getRandom(count);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void save(U uniqueObject) throws ValidationException, ServiceException {
    try {
      repository.save(uniqueObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  protected void setDefaultSorting(PageRequest pageRequest) {
    // business logic: default sorting if no other sorting given: lastModified descending, uuid
    // ascending
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(new Order(Direction.DESC, "lastModified"), new Order("uuid"));
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public void update(U uniqueObject) throws ValidationException, ServiceException {
    try {
      repository.update(uniqueObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }
}
