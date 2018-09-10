package de.digitalcollections.cudami.admin.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.ResourceRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.ResourceService;
import de.digitalcollections.cudami.admin.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
//@Transactional(readOnly = true)
public class ResourceServiceImpl<R extends Resource> extends IdentifiableServiceImpl<R> implements ResourceService<R> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);

  @Autowired
  public ResourceServiceImpl(@Qualifier("resourceRepositoryImpl") ResourceRepository<R> repository) {
    super(repository);
  }

  @Override
  public R save(FileResource fileResource, byte[] bytes, Errors results) throws IdentifiableServiceException {
    Resource resource = null;
    if (!results.hasErrors()) {
      try {
        resource = (R) ((ResourceRepository) repository).save(fileResource, bytes);
      } catch (Exception e) {
        LOGGER.error("Cannot save fileResource " + fileResource + ": ", e);
        throw new IdentifiableServiceException(e.getMessage());
      }
    }
    // FIXME: what if results has errors? throw exception?
    return (R) resource;
  }
}
