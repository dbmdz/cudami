package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.cudami.server.business.impl.service.UniqueObjectServiceImpl;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("identifierTypeService")
// @Transactional(rollbackFor = {Exception.class}) //is set on super class
public class IdentifierTypeServiceImpl
    extends UniqueObjectServiceImpl<IdentifierType, IdentifierTypeRepository>
    implements IdentifierTypeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeServiceImpl.class);

  private Map<String, String> identifierTypeCache;

  public IdentifierTypeServiceImpl(IdentifierTypeRepository repository) throws ServiceException {
    super(repository);
  }

  @Override
  public IdentifierType getByNamespace(String namespace) throws ServiceException {
    try {
      return repository.getByNamespace(namespace);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public Map<String, String> getIdentifierTypeCache() throws ServiceException {
    if (identifierTypeCache == null) {
      updateIdentifierTypeCache();
    }
    return identifierTypeCache;
  }

  @Override
  public void save(IdentifierType identifierType) throws ServiceException, ValidationException {
    super.save(identifierType);
    if (identifierType != null) {
      getIdentifierTypeCache().put(identifierType.getNamespace(), identifierType.getPattern());
    }
  }

  @Override
  protected void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "namespace", "uuid");
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public void update(IdentifierType identifierType) throws ServiceException, ValidationException {
    super.update(identifierType);
    if (identifierType != null) {
      getIdentifierTypeCache().put(identifierType.getNamespace(), identifierType.getPattern());
    }
  }

  @Override
  public Map<String, String> updateIdentifierTypeCache() throws ServiceException {
    Set<IdentifierType> allIdentifierTypes = getAll();
    identifierTypeCache =
        allIdentifierTypes.stream()
            .collect(
                Collectors.toConcurrentMap(
                    IdentifierType::getNamespace, IdentifierType::getPattern));
    return identifierTypeCache;
  }
}
