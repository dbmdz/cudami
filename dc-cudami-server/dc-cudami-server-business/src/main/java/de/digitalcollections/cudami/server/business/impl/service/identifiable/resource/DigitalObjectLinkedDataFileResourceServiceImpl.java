package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectLinkedDataFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
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
  private LinkedDataFileResourceService linkedDataFileResourceService;

  public DigitalObjectLinkedDataFileResourceServiceImpl(
      DigitalObjectLinkedDataFileResourceRepository linkedDataFileResourceRepository,
      LinkedDataFileResourceService linkedDataFileResourceService) {
    this.repository = linkedDataFileResourceRepository;
    this.linkedDataFileResourceService = linkedDataFileResourceService;
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

  @Override
  public void deleteLinkedDataFileResources(UUID digitalObjectUuid) throws CudamiServiceException {
    List<LinkedDataFileResource> linkedDataFileResources =
        getLinkedDataFileResources(digitalObjectUuid);
    if (linkedDataFileResources == null || linkedDataFileResources.isEmpty()) {
      return;
    }

    for (LinkedDataFileResource linkedDataFileResource : linkedDataFileResources) {
      try {
        // Delete the relation
        repository.delete(linkedDataFileResource.getUuid());

        // Delete the resource, when no references exist to it
        if (repository.countDigitalObjectsForResource(linkedDataFileResource.getUuid()) == 0) {
          linkedDataFileResourceService.delete(linkedDataFileResource.getUuid());
        }
      } catch (Exception e) {
        throw new CudamiServiceException(
            "Cannot delete LinkedDataFileResource="
                + linkedDataFileResource
                + " for DigitalObject with uuid="
                + digitalObjectUuid
                + ": "
                + e,
            e);
      }
    }
  }
}
