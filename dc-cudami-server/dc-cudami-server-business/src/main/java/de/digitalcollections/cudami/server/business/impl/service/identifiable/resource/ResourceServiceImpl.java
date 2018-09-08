package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ResourceService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.resource.Resource;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
//@Transactional(readOnly = true)
public class ResourceServiceImpl<R extends Resource> extends IdentifiableServiceImpl<R> implements ResourceService<R> {

  @Autowired
  private LocaleService localeService;

  @Autowired
  public ResourceServiceImpl(@Qualifier("resourceRepositoryImpl") ResourceRepository<R> repository) {
    super(repository);
  }

  @Override
  public R get(UUID uuid, Locale locale) throws IdentifiableServiceException {
    R resource = repository.findOne(uuid, locale);

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
}
