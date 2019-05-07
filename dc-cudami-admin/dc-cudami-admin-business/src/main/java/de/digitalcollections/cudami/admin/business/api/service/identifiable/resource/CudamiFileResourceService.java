package de.digitalcollections.cudami.admin.business.api.service.identifiable.resource;

import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.IdentifiableService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import java.io.InputStream;

public interface CudamiFileResourceService extends IdentifiableService<FileResource> {

  FileResource save(FileResource fileResource, byte[] bytes) throws IdentifiableServiceException;

  public FileResource upload(InputStream inputStream, String filename, String contentType) throws ResourceIOException;

  public FileResource upload(byte[] bytes, String filename, String contentType) throws ResourceIOException;
}
