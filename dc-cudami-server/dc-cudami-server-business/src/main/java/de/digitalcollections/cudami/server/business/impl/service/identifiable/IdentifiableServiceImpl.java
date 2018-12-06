package de.digitalcollections.cudami.server.business.impl.service.identifiable;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifiableRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.Identifiable;
import de.digitalcollections.model.api.paging.PageRequest;
import de.digitalcollections.model.api.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
//@Transactional(readOnly = true)
public class IdentifiableServiceImpl<I extends Identifiable> implements IdentifiableService<I> {

  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifiableServiceImpl.class);

  @Autowired
  private LocaleService localeService;

  protected IdentifiableRepository<I> repository;

  @Autowired
  public IdentifiableServiceImpl(@Qualifier("identifiableRepositoryImpl") IdentifiableRepository<I> repository) {
    this.repository = repository;
  }

  @Override
  public long count() {
    return repository.count();
  }

  @Override
  public PageResponse<I> find(PageRequest pageRequest) {
    return repository.find(pageRequest);
  }

  @Override
  public List<I> find(String searchTerm, int maxResults) {
    return repository.find(searchTerm, maxResults);
  }

  @Override
  public I get(UUID uuid) {
    return (I) repository.findOne(uuid);
  }

  @Override
  public I get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    I resource = repository.findOne(uuid, locale);

    // webpage does not exist in requested language, so try with default locale
    if (resource == null) {
      resource = repository.findOne(uuid, localeService.getDefault());
    }

    // webpage does not exist in default locale, so just return first existing language
    if (resource == null) {
      resource = repository.findOne(uuid, null);
    }

    return resource;
  }

  @Override
  //  @Transactional(readOnly = false)
  public I save(I identifiable) throws IdentifiableServiceException {
    try {
      return (I) repository.save(identifiable);
    } catch (Exception e) {
      LOGGER.error("Cannot save identifiable " + identifiable + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }

  @Override
  //  @Transactional(readOnly = false)
  public I update(I identifiable) throws IdentifiableServiceException {
    try {
      return (I) repository.update(identifiable);
    } catch (Exception e) {
      LOGGER.error("Cannot update identifiable " + identifiable + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
