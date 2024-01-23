package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service("identifierService")
// @Transactional(rollbackFor = {Exception.class}) //is set on super class
public class IdentifierServiceImpl extends UniqueObjectServiceImpl<Identifier, IdentifierRepository>
    implements IdentifierService {
  private final IdentifierTypeService identifierTypeService;

  public IdentifierServiceImpl(
      IdentifierRepository identifierRepository, IdentifierTypeService identifierTypeService) {
    super(identifierRepository);
    this.identifierTypeService = identifierTypeService;
  }

  @Override
  public int deleteByIdentifiable(Identifiable identifiable) throws ServiceException {
    try {
      return repository.deleteByIdentifiable(identifiable);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public List<Identifier> findByIdentifiable(Identifiable identifiable) throws ServiceException {
    try {
      return repository.findByIdentifiable(identifiable);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Set<Identifier> saveForIdentifiable(Identifiable identifiable, Set<Identifier> identifiers)
      throws ServiceException, ValidationException {
    try {
      return repository.saveForIdentifiable(identifiable, identifiers);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void validate(Set<Identifier> identifiers) throws ValidationException, ServiceException {
    // FIXME: do not get cache, get complete list of identifierTypes
    Map<String, String> identifierTypes = identifierTypeService.getIdentifierTypeCache();
    List<String> namespacesNotFound = new ArrayList<>(0);
    List<String> idsNotMatchingPattern = new ArrayList<>(0);
    boolean cacheUpdated = false;
    for (Identifier identifier : identifiers) {
      String namespace = identifier.getNamespace();
      String pattern = identifierTypes.get(namespace);
      if (pattern == null && !cacheUpdated) {
        // FIXME: should not result in update! the repo has to update the cache every
        // time
        // inserts/updates/deletes happen!
        identifierTypes = identifierTypeService.updateIdentifierTypeCache();
        cacheUpdated = true;
        pattern = identifierTypes.get(namespace);
      }
      if (pattern == null) {
        namespacesNotFound.add(namespace);
        continue;
      }
      String id = identifier.getId();
      if (id == null || !id.matches(pattern)) {
        idsNotMatchingPattern.add(namespace + ":" + id);
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
