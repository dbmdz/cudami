package de.digitalcollections.cudami.admin.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;

public interface CudamiFileResourceService<F extends FileResource> extends IdentifiableService<F> {

  public F save(FileResource fileResource, byte[] bytes) throws IdentifiableServiceException;
}
