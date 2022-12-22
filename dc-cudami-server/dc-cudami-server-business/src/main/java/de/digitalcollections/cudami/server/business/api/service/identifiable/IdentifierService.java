package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IdentifierService {
  public void delete(Set<Identifier> identifiers) throws ServiceException;

  public int deleteByIdentifiable(UUID identifiableUuid) throws ServiceException;

  public List<Identifier> findByIdentifiable(UUID uuidIdentifiable) throws ServiceException;

  public void save(Identifier identifier) throws ServiceException;

  public Set<Identifier> saveForIdentifiable(UUID identifiableUuid, Set<Identifier> identifiers)
      throws ServiceException;

  public void validate(Set<Identifier> identifiers) throws ServiceException, ValidationException;
}
