package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;

public interface PagingSortingFilteringRepository<O extends Object> {
  /**
   * Get paged, sorted, filtered objects
   *
   * @param pageRequest request param container for paging, sorting, filtering
   * @return result as paged response
   */
  PageResponse<O> find(PageRequest pageRequest);
}
