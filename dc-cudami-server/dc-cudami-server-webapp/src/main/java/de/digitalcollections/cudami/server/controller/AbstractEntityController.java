package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.EntityService;
import de.digitalcollections.cudami.server.controller.identifiable.AbstractIdentifiableController;
import de.digitalcollections.model.identifiable.entity.Entity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class AbstractEntityController<E extends Entity>
    extends AbstractIdentifiableController<E> {

  @Override
  protected abstract EntityService<E> getService();

  protected ResponseEntity<E> getByRefId(long refId) throws ServiceException {
    E entity = getService().getByRefId(refId);
    return new ResponseEntity<>(entity, entity != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
  }
}
