package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ResourceService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.resource.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
//@Transactional(readOnly = true)
public class ResourceServiceImpl<R extends Resource> extends IdentifiableServiceImpl<R> implements ResourceService<R> {

  @Autowired
  public ResourceServiceImpl(@Qualifier("resourceRepositoryImpl") ResourceRepository<R> repository) {
    super(repository);
  }
}
