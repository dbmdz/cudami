package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.List;
import java.util.UUID;

/** Service for LinkedDataFileResource handling. */
public interface DigitalObjectLinkedDataFileResourceService {

  /**
   * Returns the list of LinkedDataFileResources for a DigitalObject, identified by its UUID
   *
   * @param digitalObjectUuid the UUID of the DigitalObject
   * @return list of LinkedDataFileResources
   */
  List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid);

  /**
   * Saves the list of LinkedDataFileResources for a DigitalObject, identified by its UUID
   *
   * @param digitalObjectUuid the UUID of the DigitalObject
   * @param linkedDataFileResources list of LinkedDataFileResources to be persisted
   * @return list of persisted LinkedDataFileResources
   * @throws ServiceException
   */
  List<LinkedDataFileResource> setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources)
      throws ServiceException;

  void deleteLinkedDataFileResources(UUID digitalObjectUuid) throws CudamiServiceException;
}
