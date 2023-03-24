package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;

public interface PagingSortingFilteringService<O extends Object> {
  PageResponse<O> find(PageRequest pageRequest);
}
