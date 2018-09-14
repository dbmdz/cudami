package de.digitalcollections.cudami.server.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.model.api.identifiable.resource.FileResource;

public interface FileResourceService<F extends FileResource> extends ResourceService<F> {

  F save(F fileResource, byte[] binaryData) throws IdentifiableServiceException;
}
