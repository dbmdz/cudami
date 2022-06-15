package de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource;

import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import java.util.List;
import java.util.UUID;

/** Repository for LinkedDataFileResource persistence handling. */
public interface DigitalObjectLinkedDataFileResourceRepository {

  /**
   * Retrieve all LinkedDataFileResources for a DigitalObject, identified by its UUID
   *
   * @param digitalObjectUuid the UUID of the DigitalObject
   * @return List of LinkedDataFileResources
   */
  List<LinkedDataFileResource> getLinkedDataFileResources(UUID digitalObjectUuid);

  /**
   * Save all LinkedDataFileResources for a DigitalObject, identified by its UUID
   *
   * @param digitalObjectUuid the UUID of the DigitalObject
   * @param linkedDataFileResources List of LinkedDataFileResource to persist
   * @return
   */
  List<LinkedDataFileResource> setLinkedDataFileResources(
      UUID digitalObjectUuid, List<LinkedDataFileResource> linkedDataFileResources);

  default int delete(UUID uuid) {
    return delete(List.of(uuid)); // same performance as "where uuid = :uuid"
  }

  int delete(List<UUID> uuids);

  int countDigitalObjectsForResource(UUID uuid);
}
