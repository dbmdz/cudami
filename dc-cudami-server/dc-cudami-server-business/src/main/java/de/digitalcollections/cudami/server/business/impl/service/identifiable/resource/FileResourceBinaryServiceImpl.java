package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceBinaryRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceBinaryService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileResourceBinaryServiceImpl implements FileResourceBinaryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourceBinaryServiceImpl.class);

  private final FileResourceBinaryRepository binaryRepository;
  private final FileResourceMetadataRepository metadataRepository;

  @Autowired
  public FileResourceBinaryServiceImpl(
      FileResourceMetadataRepository metadataRepository,
      FileResourceBinaryRepository binaryRepository) {
    this.binaryRepository = binaryRepository;
    this.metadataRepository = metadataRepository;
  }

  @Override
  public FileResource save(FileResource fileResource, InputStream binaryData)
      throws IdentifiableServiceException {
    try {
      fileResource = binaryRepository.save(fileResource, binaryData);
      fileResource = metadataRepository.save(fileResource);
      return fileResource;
    } catch (ResourceIOException e) {
      LOGGER.error("Cannot save fileResource " + fileResource.getFilename() + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
