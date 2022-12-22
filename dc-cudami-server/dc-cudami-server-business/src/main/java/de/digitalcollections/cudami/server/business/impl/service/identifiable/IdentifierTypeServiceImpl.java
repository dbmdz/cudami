package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Sorting;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("identifierTypeService")
@Transactional(rollbackFor = {Exception.class})
public class IdentifierTypeServiceImpl implements IdentifierTypeService {

  private final IdentifierTypeRepository repository;
  private Map<String, String> identifierTypeCache;

  @Autowired
  public IdentifierTypeServiceImpl(IdentifierTypeRepository repository) throws ServiceException {
    this.repository = repository;
    updateIdentifierTypeCache();
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public void delete(List<UUID> uuids) {
    repository.delete(uuids);
  }

  @Override
  public PageResponse<IdentifierType> find(PageRequest pageRequest) {
    setDefaultSorting(pageRequest);
    return repository.find(pageRequest);
  }

  @Override
  public IdentifierType getByNamespace(String namespace) {
    return repository.getByNamespace(namespace);
  }

  @Override
  public IdentifierType getByUuid(UUID uuid) {
    return repository.getByUuid(uuid);
  }

  @Override
  public Map<String, String> getIdentifierTypeCache() {
    return identifierTypeCache;
  }

  @Override
  public IdentifierType save(IdentifierType identifierType) throws ServiceException {
    IdentifierType saved;
    try {
      saved = repository.save(identifierType);
    } catch (RepositoryException e) {
      throw new ServiceException("Cannot save IdentifierType: " + identifierType.toString(), e);
    }
    if (saved != null) {
      identifierTypeCache.put(saved.getNamespace(), saved.getPattern());
    }
    return saved;
  }

  private void setDefaultSorting(PageRequest pageRequest) {
    if (!pageRequest.hasSorting()) {
      Sorting sorting = new Sorting(Direction.ASC, "namespace", "uuid");
      pageRequest.setSorting(sorting);
    }
  }

  @Override
  public IdentifierType update(IdentifierType identifierType) throws ServiceException {
    IdentifierType updated;
    try {
      updated = repository.update(identifierType);
    } catch (RepositoryException e) {
      throw new ServiceException("Cannot update IdentifierType: " + identifierType.toString(), e);
    }
    if (updated != null) {
      identifierTypeCache.put(updated.getNamespace(), updated.getPattern());
    }
    return updated;
  }

  @Override
  public Map<String, String> updateIdentifierTypeCache() throws ServiceException {
    try {
      identifierTypeCache =
          repository.findAll().stream()
              .collect(
                  Collectors.toConcurrentMap(
                      IdentifierType::getNamespace, IdentifierType::getPattern));
    } catch (RepositoryException e) {
      throw new ServiceException(e);
    }
    return identifierTypeCache;
  }
}
