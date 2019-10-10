package de.digitalcollections.cudami.admin.business.impl.service.identifiable;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.IdentifierTypeRepository;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.model.api.identifiable.IdentifierType;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
// @Transactional(readOnly = true)
public class IdentierTypeServiceImpl implements IdentifierTypeService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentierTypeServiceImpl.class);

  protected IdentifierTypeRepository repository;

  @Autowired
  public IdentierTypeServiceImpl(
      @Qualifier("identifierTypeRepositoryImpl") IdentifierTypeRepository repository) {
    this.repository = repository;
  }

  @Override
  public IdentifierType create() {
    IdentifierType identifierType = (IdentifierType) repository.create();
    return identifierType;
  }

  @Override
  public PageResponse<IdentifierType> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public IdentifierType get(UUID uuid) {
    return (IdentifierType) repository.findOne(uuid);
  }

  @Override
  @Transactional(readOnly = false)
  public IdentifierType save(IdentifierType identifierType) {
    identifierType = (IdentifierType) repository.save(identifierType);
    return identifierType;
  }

  @Override
  @Transactional(readOnly = false)
  public IdentifierType update(IdentifierType identifierType) {
    identifierType = (IdentifierType) repository.update(identifierType);
    return identifierType;
  }
}
