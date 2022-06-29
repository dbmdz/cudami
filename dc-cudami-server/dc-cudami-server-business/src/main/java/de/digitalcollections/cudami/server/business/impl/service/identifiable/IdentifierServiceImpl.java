package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service("identifierService")
public class IdentifierServiceImpl implements IdentifierService {
  private final IdentifierRepository identifierRepository;
  private final IdentifierTypeServiceImpl identifierTypeService;

  public IdentifierServiceImpl(
      IdentifierRepository identifierRepository, IdentifierTypeServiceImpl identifierTypeService) {
    this.identifierRepository = identifierRepository;
    this.identifierTypeService = identifierTypeService;
  }

  @Override
  public void delete(Set<Identifier> identifiers) {
    List<UUID> uuids = identifiers.stream().map(i -> i.getUuid()).collect(Collectors.toList());
    identifierRepository.delete(uuids);
  }

  @Override
  public int deleteByIdentifiable(UUID identifiableUuid) {
    return identifierRepository.deleteByIdentifiable(identifiableUuid);
  }

  @Override
  public List<Identifier> findByIdentifiable(UUID uuidIdentifiable) {
    return identifierRepository.findByIdentifiable(uuidIdentifiable);
  }

  @Override
  public Identifier save(Identifier identifier) {
    return identifierRepository.save(identifier);
  }

  @Override
  public Set<Identifier> saveForIdentifiable(UUID identifiableUuid, Set<Identifier> identifiers) {
    Set<Identifier> savedIdentifiers = new HashSet<>(0);
    if (identifiers != null) {
      for (Identifier identifier : identifiers) {
        identifier.setIdentifiable(identifiableUuid);
        Identifier savedIdentifier;
        if (identifier.getUuid() == null) {
          savedIdentifier = identifierRepository.save(identifier);
        } else {
          savedIdentifier = identifierRepository.getByUuid(identifier.getUuid());
        }
        savedIdentifiers.add(savedIdentifier);
      }
    }
    return savedIdentifiers;
  }

  @Override
  public void validate(Set<Identifier> identifiers) throws ValidationException {
    Map<String, String> identifierTypes = identifierTypeService.getIdentifierTypeCache();
    List<String> namespacesNotFound = new ArrayList<>(0);
    List<String> idsNotMatchingPattern = new ArrayList<>(0);
    for (Identifier identifier : identifiers) {
      String namespace = identifier.getNamespace();
      String pattern = identifierTypes.get(identifier.getNamespace());
      if (pattern == null) {
        namespacesNotFound.add(namespace);
        continue;
      }
      String id = identifier.getId();
      if (!id.matches(pattern)) {
        idsNotMatchingPattern.add(id);
      }
    }
    if (namespacesNotFound.isEmpty() && idsNotMatchingPattern.isEmpty()) {
      return;
    }
    throw new ValidationException(
        "Validation of identifiers failed: namespacesNotFound="
            + namespacesNotFound
            + ", idsNotMatchingPattern="
            + idsNotMatchingPattern);
  }
}
