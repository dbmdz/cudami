package de.digitalcollections.cudami.server.controller;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.model.UniqueObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractUniqueObjectController<U extends UniqueObject>
    extends AbstractPagingAndSortingController {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AbstractUniqueObjectController.class);

  protected abstract UniqueObjectService<U> getService();
}
