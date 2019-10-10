package de.digitalcollections.cudami.admin.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.cudami.admin.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.CudamiFileResourceService;
import de.digitalcollections.cudami.admin.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
// @Transactional(readOnly = true)
public class CudamiFileResourceServiceImpl extends IdentifiableServiceImpl<FileResource>
    implements CudamiFileResourceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiFileResourceServiceImpl.class);

  @Autowired
  public CudamiFileResourceServiceImpl(CudamiFileResourceRepository repository) {
    super(repository);
  }

  @Override
  public FileResource save(FileResource fileResource, byte[] bytes)
      throws IdentifiableServiceException {
    try {
      // FIXME save/upload over feign endpoint does not work (anymore?)
      //      fileResource = ((CudamiFileResourceRepository) repository).save(fileResource, bytes);
      fileResource =
          upload(bytes, fileResource.getFilename(), fileResource.getMimeType().getTypeName());
    } catch (Exception e) {
      LOGGER.error("Cannot save fileResource " + fileResource + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
    return fileResource;
  }

  @Override
  public FileResource upload(InputStream inputStream, String filename, String contentType)
      throws ResourceIOException {
    return ((CudamiFileResourceRepository) repository).upload(inputStream, filename, contentType);
  }

  @Override
  public FileResource upload(byte[] bytes, String filename, String contentType)
      throws ResourceIOException {
    return ((CudamiFileResourceRepository) repository).upload(bytes, filename, contentType);
  }
}
