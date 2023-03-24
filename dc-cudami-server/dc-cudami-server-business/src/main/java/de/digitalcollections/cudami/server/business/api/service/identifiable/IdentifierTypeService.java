package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.IdentifierType;
import java.util.Map;

public interface IdentifierTypeService extends UniqueObjectService<IdentifierType> {

  IdentifierType getByNamespace(String namespace);

  Map<String, String> getIdentifierTypeCache();

  Map<String, String> updateIdentifierTypeCache() throws ServiceException;
}
