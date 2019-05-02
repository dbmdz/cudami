package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Service for Digital Object.
 */
public interface DigitalObjectService extends EntityService<DigitalObject> {

  LinkedHashSet<FileResource> getFileResources(DigitalObject digitalObject);

  LinkedHashSet<FileResource> getFileResources(UUID digitalObjectUuid);

  LinkedHashSet<FileResource> saveFileResources(DigitalObject digitalObject, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveFileResources(UUID digitalObjectUuid, LinkedHashSet<FileResource> fileResources);
}
