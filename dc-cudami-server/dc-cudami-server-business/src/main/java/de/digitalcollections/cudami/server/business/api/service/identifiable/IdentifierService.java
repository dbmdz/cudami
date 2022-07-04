package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IdentifierService {
  public void delete(Set<Identifier> identifiers) throws CudamiServiceException;

  public int deleteByIdentifiable(UUID identifiableUuid) throws CudamiServiceException;

  public List<Identifier> findByIdentifiable(UUID uuidIdentifiable) throws CudamiServiceException;

  public Identifier save(Identifier identifier) throws CudamiServiceException;

  public Set<Identifier> saveForIdentifiable(UUID identifiableUuid, Set<Identifier> identifiers)
      throws CudamiServiceException;

  public void validate(Set<Identifier> identifiers)
      throws CudamiServiceException, ValidationException;
}
