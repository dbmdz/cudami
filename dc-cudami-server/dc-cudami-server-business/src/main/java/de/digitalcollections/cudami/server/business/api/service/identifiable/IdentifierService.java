package de.digitalcollections.cudami.server.business.api.service.identifiable;

import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IdentifierService {
  public void delete(Set<Identifier> identifiers);

  public int deleteByIdentifiable(UUID identifiableUuid);

  public List<Identifier> findByIdentifiable(UUID uuidIdentifiable);

  public Identifier save(Identifier identifier);

  public Set<Identifier> saveForIdentifiable(UUID identifiableUuid, Set<Identifier> identifiers);

  public void validate(Set<Identifier> identifiers) throws ValidationException;
}
