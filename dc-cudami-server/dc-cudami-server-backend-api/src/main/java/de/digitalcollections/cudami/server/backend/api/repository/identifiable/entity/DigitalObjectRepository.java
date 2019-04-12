package de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity;

import de.digitalcollections.model.api.identifiable.entity.DigitalObject;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * Repository for Digital object persistence handling.
 *
 * @param <D> digital object
 */
public interface DigitalObjectRepository<D extends DigitalObject> extends EntityRepository<D> {

  LinkedHashSet<FileResource> getFileResources(D digitalObject);

  LinkedHashSet<FileResource> getFileResources(UUID digitalObjectUuid);

  LinkedHashSet<FileResource> saveFileResources(D digitalObject, LinkedHashSet<FileResource> fileResources);

  LinkedHashSet<FileResource> saveFileResources(UUID digitalObjectUuid, LinkedHashSet<FileResource> fileResources);
}
