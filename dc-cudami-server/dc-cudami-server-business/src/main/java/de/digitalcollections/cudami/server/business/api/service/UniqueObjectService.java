package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;

public interface UniqueObjectService<U extends UniqueObject> {

  PageResponse<U> find(PageRequest pageRequest);
}
