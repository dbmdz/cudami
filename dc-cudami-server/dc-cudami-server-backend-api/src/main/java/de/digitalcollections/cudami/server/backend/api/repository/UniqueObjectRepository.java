package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.model.UniqueObject;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;

public interface UniqueObjectRepository<U extends UniqueObject> {

  PageResponse<U> find(PageRequest pageRequest);

  default U getByUuid(UUID uuid) {
    return getByUuidAndFiltering(uuid, null);
  }

  U getByUuidAndFiltering(UUID uuid, Filtering filtering);
}
