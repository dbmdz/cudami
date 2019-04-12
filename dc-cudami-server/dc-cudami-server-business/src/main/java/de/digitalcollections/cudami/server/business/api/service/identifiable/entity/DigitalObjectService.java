package de.digitalcollections.cudami.server.business.api.service.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Service for Digital Object.
 *
 * @param <D> domain object
 */
public interface DigitalObjectService<D extends DigitalObject> extends EntityService<D> {

  LinkedHashSet<FileResource> getFileResources(D digitalObject);

  LinkedHashSet<FileResource> getFileResources(UUID digitalObjectUuid);

  LinkedHashSet<FileResource> saveFileResources(D digitalObject, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveFileResources(UUID digitalObjectUuid, LinkedHashSet<FileResource> fileResources);
}
