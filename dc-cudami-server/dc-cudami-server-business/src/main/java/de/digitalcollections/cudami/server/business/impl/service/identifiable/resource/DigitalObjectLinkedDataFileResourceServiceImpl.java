package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectLinkedDataFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DigitalObjectLinkedDataFileResourceServiceImpl
    implements DigitalObjectLinkedDataFileResourceService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DigitalObjectLinkedDataFileResourceServiceImpl.class);

  private DigitalObjectLinkedDataFileResourceRepository repository;

  public DigitalObjectLinkedDataFileResourceServiceImpl(
      DigitalObjectLinkedDataFileResourceRepository linkedDataFileResourceRepository) {
    this.repository = linkedDataFileResourceRepository;
  }

  @Override
  public List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid) {
    return repository.getLinkedDataFileResources(digitalObjectUuid);
  }

  @Override
  public List<LinkedDataFileResource> setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources) {
    return repository.setLinkedDataFileResources(digitalObjectUuid, linkedDataFileResources);
  }
}
