package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class FileResourceServiceImpl<F extends FileResource> extends ResourceServiceImpl<F> implements FileResourceService<F> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourceServiceImpl.class);

  @Autowired
  public FileResourceServiceImpl(@Qualifier("fileResourceRepositoryImpl") FileResourceRepository<F> repository) {
    super(repository);
  }

  @Override
  public F save(F fileResource, byte[] binaryData) throws IdentifiableServiceException {
    try {
      return (F) ((FileResourceRepository) repository).save(fileResource, binaryData);
    } catch (Exception e) {
      LOGGER.error("Cannot save fileResource " + fileResource.getFilename() + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
