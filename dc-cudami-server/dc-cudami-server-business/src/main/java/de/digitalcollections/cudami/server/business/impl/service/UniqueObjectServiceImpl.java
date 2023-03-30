package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.cudami.server.backend.api.repository.UniqueObjectRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;

public abstract class UniqueObjectServiceImpl<
        U extends UniqueObject, R extends UniqueObjectRepository<U>>
    implements UniqueObjectService<U> {

  protected R repository;

  protected UniqueObjectServiceImpl(R repository) {
    this.repository = repository;
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

  protected void setDefaultSorting(PageRequest pageRequest) {
    // business logic: default sorting if no other sorting given: lastModified descending, uuid
    // ascending
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(new Order(Direction.DESC, "lastModified"), new Order("uuid"));
      pageRequest.setSorting(sorting);
    }
  }
}
