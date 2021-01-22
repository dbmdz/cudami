package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdentifierTypeServiceImpl implements IdentifierTypeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeServiceImpl.class);

  protected IdentifierTypeRepository repository;

  @Autowired
  public IdentifierTypeServiceImpl(IdentifierTypeRepository repository) {
    this.repository = repository;
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
    return repository.find(pageRequest);
  }

  @Override
  public IdentifierType get(UUID uuid) {
    return repository.findOne(uuid);
  }

  @Override
  public IdentifierType getByNamespace(String namespace) {
    return repository.findOneByNamespace(namespace);
  }

  @Override
  public IdentifierType save(IdentifierType identifierType) {
    return repository.save(identifierType);
  }

  @Override
  public IdentifierType update(IdentifierType identifiable) {
    return repository.update(identifiable);
  }
}
