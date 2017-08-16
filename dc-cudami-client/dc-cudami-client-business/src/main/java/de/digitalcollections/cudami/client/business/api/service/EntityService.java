package de.digitalcollections.cudami.client.business.api.service;

import de.digitalcollections.core.model.api.paging.PageRequest;
import de.digitalcollections.core.model.api.paging.PageResponse;
import de.digitalcollections.cudami.client.business.api.service.exceptions.EntityServiceException;
import de.digitalcollections.cudami.model.api.entity.Entity;
import java.io.Serializable;
import org.springframework.validation.Errors;

public interface EntityService<E extends Entity, ID extends Serializable> {

  long count();

  E create();

  PageResponse<E> find(PageRequest pageRequest);

  E get(ID id);

  E save(E user, Errors results) throws EntityServiceException;

  E update(E user, Errors results) throws EntityServiceException;
}
