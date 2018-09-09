package de.digitalcollections.cudami.admin.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.Resource;
import org.springframework.validation.Errors;

public interface ResourceService<R extends Resource> extends IdentifiableService<R> {

  public R save(R resource, FileResource fileResource, byte[] bytes, Errors results) throws IdentifiableServiceException;

}
