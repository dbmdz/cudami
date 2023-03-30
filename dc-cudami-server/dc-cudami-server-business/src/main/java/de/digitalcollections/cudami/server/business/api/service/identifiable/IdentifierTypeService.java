package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.IdentifierType;
import java.util.Map;

public interface IdentifierTypeService extends UniqueObjectService<IdentifierType> {

  IdentifierType getByNamespace(String namespace) throws ServiceException;

  // FIXME: move as internal implementation to IdentifierTypeRepositoryImpl or IdentifierServiceImpl
  // (for validation only)
  // get all identifierTypes using count and paging (maybe introduce a getAll() in repo?)
  Map<String, String> getIdentifierTypeCache();

  Map<String, String> updateIdentifierTypeCache() throws ServiceException;
}
