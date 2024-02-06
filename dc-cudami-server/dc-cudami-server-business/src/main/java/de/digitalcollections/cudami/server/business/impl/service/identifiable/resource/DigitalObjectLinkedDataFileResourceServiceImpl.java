package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectLinkedDataFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectLinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.model.identifiable.entity.digitalobject.DigitalObject;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.validation.ValidationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DigitalObjectLinkedDataFileResourceServiceImpl
    implements DigitalObjectLinkedDataFileResourceService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(DigitalObjectLinkedDataFileResourceServiceImpl.class);

  private LinkedDataFileResourceService linkedDataFileResourceService;
  private DigitalObjectLinkedDataFileResourceRepository repository;

  public DigitalObjectLinkedDataFileResourceServiceImpl(
      DigitalObjectLinkedDataFileResourceRepository linkedDataFileResourceRepository,
      LinkedDataFileResourceService linkedDataFileResourceService) {
    this.repository = linkedDataFileResourceRepository;
    this.linkedDataFileResourceService = linkedDataFileResourceService;
  }

  @Override
  public void deleteLinkedDataFileResources(DigitalObject digitalObject) throws ServiceException {
    List<LinkedDataFileResource> linkedDataFileResources =
        getLinkedDataFileResources(digitalObject);
    if (linkedDataFileResources == null || linkedDataFileResources.isEmpty()) {
      return;
    }

    for (LinkedDataFileResource linkedDataFileResource : linkedDataFileResources) {
      try {
        // Delete the relation
        int amountDeletedRelations = repository.delete(linkedDataFileResource.getUuid());
        if (amountDeletedRelations != 1) {
          throw new ServiceException(
              "Could not delete relation for LinkedDataFileResource="
                  + linkedDataFileResource
                  + " for DigitalObject="
                  + digitalObject);
        }

        // Delete the resource, when no references exist to it
        if (repository.countDigitalObjectsForResource(linkedDataFileResource.getUuid()) == 0) {
          linkedDataFileResourceService.delete(linkedDataFileResource);
        }
      } catch (Exception e) {
        throw new ServiceException(
            "Cannot delete LinkedDataFileResource="
                + linkedDataFileResource
                + " for DigitalObject="
                + digitalObject
                + ": "
                + e,
            e);
      }
    }
  }

  @Override
  public List<LinkedDataFileResource> getLinkedDataFileResources(DigitalObject digitalObject)
      throws ServiceException {
    try {
      return repository.getLinkedDataFileResources(digitalObject);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
  }

  @Override
  public void setLinkedDataFileResources(
      DigitalObject digitalObject, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException, ValidationException {
    try {
      repository.setLinkedDataFileResources(digitalObject, linkedDataFileResources);
    } catch (RepositoryException e) {
      throw new ServiceException(
          "Cannot set linked data file resources for %s".formatted(digitalObject), e);
    }
  }
}
