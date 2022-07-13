package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.identifiable.Identifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("identifierService")
@Transactional(rollbackFor = {Exception.class})
public class IdentifierServiceImpl implements IdentifierService {
  private final IdentifierRepository identifierRepository;
  private final IdentifierTypeService identifierTypeService;

  public IdentifierServiceImpl(
      IdentifierRepository identifierRepository, IdentifierTypeService identifierTypeService) {
    this.identifierRepository = identifierRepository;
    this.identifierTypeService = identifierTypeService;
  }

  @Override
  public void delete(Set<Identifier> identifiers) throws CudamiServiceException {
    try {
      List<UUID> uuids = identifiers.stream().map(i -> i.getUuid()).collect(Collectors.toList());
      identifierRepository.delete(uuids);
    } catch (RepositoryException e) {
      throw new CudamiServiceException(e);
    }
  }

  @Override
  public int deleteByIdentifiable(UUID identifiableUuid) throws CudamiServiceException {
    try {
      return identifierRepository.deleteByIdentifiable(identifiableUuid);
    } catch (RepositoryException e) {
      throw new CudamiServiceException(e);
    }
  }

  @Override
  public List<Identifier> findByIdentifiable(UUID uuidIdentifiable) throws CudamiServiceException {
    try {
      return identifierRepository.findByIdentifiable(uuidIdentifiable);
    } catch (RepositoryException e) {
      throw new CudamiServiceException(e);
    }
  }

  @Override
  public Identifier save(Identifier identifier) throws CudamiServiceException {
    try {
      return identifierRepository.save(identifier);
    } catch (RepositoryException e) {
      throw new CudamiServiceException(e);
    }
  }

  @Override
  public Set<Identifier> saveForIdentifiable(UUID identifiableUuid, Set<Identifier> identifiers)
      throws CudamiServiceException {
    Set<Identifier> savedIdentifiers = new HashSet<>(0);
    if (identifiers != null) {
      for (Identifier identifier : identifiers) {
        try {
          identifier.setIdentifiable(identifiableUuid);
          Identifier savedIdentifier;
          if (identifier.getUuid() == null) {
            savedIdentifier = identifierRepository.save(identifier);
          } else {
            savedIdentifier = identifierRepository.getByUuid(identifier.getUuid());
          }
          savedIdentifiers.add(savedIdentifier);
        } catch (RepositoryException e) {
          throw new CudamiServiceException("Cannot save identifier " + identifier + ": " + e, e);
        }
      }
    }
    return savedIdentifiers;
  }

  @Override
  public void validate(Set<Identifier> identifiers)
      throws ValidationException, CudamiServiceException {
    Map<String, String> identifierTypes = identifierTypeService.getIdentifierTypeCache();
    List<String> namespacesNotFound = new ArrayList<>(0);
    List<String> idsNotMatchingPattern = new ArrayList<>(0);
    boolean cacheUpdated = false;
    for (Identifier identifier : identifiers) {
      String namespace = identifier.getNamespace();
      String pattern = identifierTypes.get(namespace);
      if (pattern == null && !cacheUpdated) {
        identifierTypes = identifierTypeService.updateIdentifierTypeCache();
        cacheUpdated = true;
        pattern = identifierTypes.get(namespace);
      }
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
