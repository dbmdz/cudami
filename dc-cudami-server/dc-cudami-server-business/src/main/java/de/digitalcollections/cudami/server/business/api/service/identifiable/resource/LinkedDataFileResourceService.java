package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.List;
import java.util.UUID;

/** Service for LinkedDataFileResource handling. */
public interface LinkedDataFileResourceService
    extends FileResourceMetadataService<LinkedDataFileResource> {

  /**
   * Returns the list of LinkedDataFileResources for a DigitalObject, identified by its UUID
   *
   * @param digitalObjectUuid the UUID of the DigitalObject
   * @return list of LinkedDataFileResources
   */
  List<LinkedDataFileResource> getLinkedDataFileResourcesForDigitalObjectUuid(
      UUID digitalObjectUuid);

  /**
   * Saves the list of LinkedDataFileResources for a DigitalObject, identified by its UUID
   *
   * @param digitalObjectUuid the UUID of the DigitalObject
   * @param linkedDataFileResources list of LinkedDataFileResources to be persisted
   * @return list of persisted LinkedDataFileResources
   */
  List<LinkedDataFileResource> saveLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources);
}
