package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.CudamiFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.CudamiFileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CudamiFileResourceServiceImpl extends IdentifiableServiceImpl<FileResource> implements CudamiFileResourceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CudamiFileResourceServiceImpl.class);

  @Autowired
  public CudamiFileResourceServiceImpl(CudamiFileResourceRepository repository) {
    super(repository);
  }

  @Override
  public FileResource save(FileResource fileResource, InputStream binaryData) throws IdentifiableServiceException {
    try {
      return ((CudamiFileResourceRepository) repository).save(fileResource, binaryData);
    } catch (Exception e) {
      LOGGER.error("Cannot save fileResource " + fileResource.getFilename() + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
