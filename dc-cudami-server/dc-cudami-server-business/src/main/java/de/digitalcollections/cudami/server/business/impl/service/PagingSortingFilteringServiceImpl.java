package de.digitalcollections.cudami.server.business.impl.service;

import de.digitalcollections.cudami.server.backend.api.repository.PagingSortingFilteringRepository;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.business.api.service.PagingSortingFilteringService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;

public class PagingSortingFilteringServiceImpl<
        O extends Object, R extends PagingSortingFilteringRepository<O>>
    implements PagingSortingFilteringService<O> {

  protected R repository;

  protected PagingSortingFilteringServiceImpl(R repository) {
    this.repository = repository;
  }

  @Override
  public PageResponse<O> find(PageRequest pageRequest) throws ServiceException {
    try {
      PageResponse<O> response = repository.find(pageRequest);
      return response;
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }
}
