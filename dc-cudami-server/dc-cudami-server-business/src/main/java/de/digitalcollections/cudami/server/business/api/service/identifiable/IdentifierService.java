package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.Set;

public interface IdentifierService extends UniqueObjectService<Identifier> {
  int deleteByIdentifiable(Identifiable identifiable) throws ServiceException;

  Set<Identifier> findByIdentifiable(Identifiable identifiable) throws ServiceException;

  Set<Identifier> saveForIdentifiable(Identifiable identifiable, Set<Identifier> identifiers)
      throws ServiceException;

  void validate(Set<Identifier> identifiers) throws ServiceException, ValidationException;
}
