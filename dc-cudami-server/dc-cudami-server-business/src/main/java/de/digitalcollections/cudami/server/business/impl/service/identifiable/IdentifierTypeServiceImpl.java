package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = {Exception.class})
public class IdentifierTypeServiceImpl implements IdentifierTypeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeServiceImpl.class);

  protected IdentifierTypeRepository repository;
  private Map<String, String> identifierTypeCache;

  @Autowired
  public IdentifierTypeServiceImpl(IdentifierTypeRepository repository)
      throws CudamiServiceException {
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

  public Map<String, String> getIdentifierTypeCache() {
    return identifierTypeCache;
  }

  @Override
  public IdentifierType save(IdentifierType identifierType) {
    IdentifierType saved = repository.save(identifierType);
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
  public IdentifierType update(IdentifierType identifierType) {
    IdentifierType updated = repository.update(identifierType);
    if (updated != null) {
      identifierTypeCache.put(updated.getNamespace(), updated.getPattern());
    }
    return updated;
  }

  public final Map<String, String> updateIdentifierTypeCache() throws CudamiServiceException {
    try {
      identifierTypeCache =
          repository.findAll().stream()
              .collect(
                  Collectors.toConcurrentMap(
                      IdentifierType::getNamespace, IdentifierType::getPattern));
    } catch (RepositoryException e) {
      throw new CudamiServiceException(e);
    }
    return identifierTypeCache;
  }
}
