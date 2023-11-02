package de.digitalcollections.cudami.server.business.api.service.semantic;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.semantic.Tag;

/** Service for Tag */
public interface TagService extends UniqueObjectService<Tag> {

  Tag getByValue(String value) throws ServiceException;
}
