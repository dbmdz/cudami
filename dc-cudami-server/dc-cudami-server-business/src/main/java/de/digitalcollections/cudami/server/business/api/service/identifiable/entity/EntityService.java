package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.identifiable.entity.Entity;

/**
 * @param <E> entity instance
 */
public interface EntityService<E extends Entity> extends IdentifiableService<E> {

  E getByRefId(long refId) throws ServiceException;
}
