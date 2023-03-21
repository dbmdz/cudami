package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.UniqueObjectService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.IdentifierType;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface IdentifierTypeService extends UniqueObjectService<IdentifierType> {

  long count();

  default void delete(UUID uuid) {
    delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  void delete(List<UUID> uuids);

  IdentifierType getByNamespace(String namespace);

  IdentifierType getByUuid(UUID uuid);

  IdentifierType save(IdentifierType identifierType) throws ServiceException;

  IdentifierType update(IdentifierType identifierType) throws ServiceException;

  Map<String, String> getIdentifierTypeCache();

  Map<String, String> updateIdentifierTypeCache() throws ServiceException;
}
