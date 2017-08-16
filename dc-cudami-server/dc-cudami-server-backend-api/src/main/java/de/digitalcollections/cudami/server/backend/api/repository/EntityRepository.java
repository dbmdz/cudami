package de.digitalcollections.cudami.server.backend.api.repository;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.entity.Entity;
import java.io.Serializable;

public interface EntityRepository<E extends Entity, ID extends Serializable> {

  long count();

  E create();

  PageResponse<E> find(PageRequest pageRequest);

  E findOne(ID id);

  E save(E entity);

  E update(E entity);

}
