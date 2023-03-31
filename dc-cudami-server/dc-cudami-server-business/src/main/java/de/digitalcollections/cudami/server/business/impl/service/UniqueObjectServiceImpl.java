package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.List;
import java.util.Set;

public abstract class UniqueObjectServiceImpl<
        U extends UniqueObject, R extends UniqueObjectRepository<U>>
    extends PagingSortingFilteringServiceImpl<U, R> implements UniqueObjectService<U> {

  protected UniqueObjectServiceImpl(R repository) {
    super(repository);
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
    return super.find(pageRequest);
  }

  @Override
  public U getByExample(U uniqueObject) throws ServiceException {
    try {
      return repository.getByExample(uniqueObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public U getByExampleAndFiltering(U uniqueObject, Filtering filtering) throws ServiceException {
    try {
      return repository.getByExampleAndFiltering(uniqueObject, filtering);
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

  //  @Override
  //  // FIXME: bindings?!!! try to get rid of this method...
  //  public void save(U uniqueObject, Map<String, Object> bindings)
  //      throws ValidationException, ServiceException {
  //    try {
  //      repository.save(uniqueObject, bindings);
  //    } catch (RepositoryException e) {
  //      throw new ServiceException("Backend failure", e);
  //    }
  //  }

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

  //  @Override
  //  // FIXME: bindings?!!! try to get rid of this method...
  //  public void update(U uniqueObject, Map<String, Object> bindings) throws ServiceException {
  //    try {
  //      repository.update(uniqueObject, bindings);
  //    } catch (RepositoryException e) {
  //      throw new ServiceException("Backend failure", e);
  //    }
  //  }
}
