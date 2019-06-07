package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
//@Transactional(readOnly = true)
public class IdentifierTypeServiceImpl implements IdentifierTypeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierTypeServiceImpl.class);

  protected IdentifierTypeRepository repository;

  @Autowired
  public IdentifierTypeServiceImpl(@Qualifier("identifierTypeRepositoryImpl") IdentifierTypeRepository repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public PageResponse find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public IdentifierType get(UUID uuid) {
    return (IdentifierType) repository.findOne(uuid);
  }

  @Override
  //  @Transactional(readOnly = false)
  public IdentifierType save(IdentifierType identifierType) {
    return (IdentifierType) repository.save(identifierType);
  }

  @Override
  //  @Transactional(readOnly = false)
  public IdentifierType update(IdentifierType identifiable) {
    return (IdentifierType) repository.update(identifiable);
  }
}
