package de.digitalcollections.cudami.server.business.api.service;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.model.api.entity.Entity;
import de.digitalcollections.cudami.server.business.api.service.exceptions.EntityServiceException;
import java.io.Serializable;

public interface EntityService<E extends Entity, ID extends Serializable> {

  long count();

  E create();

  PageResponse<E> find(PageRequest pageRequest);

  E get(ID id);

  E save(E entity) throws EntityServiceException;

  E update(E entity) throws EntityServiceException;

}
