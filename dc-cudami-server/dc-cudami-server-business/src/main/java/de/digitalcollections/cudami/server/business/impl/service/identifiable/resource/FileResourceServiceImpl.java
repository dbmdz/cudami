package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceBinaryRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.Identifier;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import de.digitalcollections.model.impl.identifiable.IdentifierImpl;
import java.io.InputStream;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileResourceServiceImpl extends IdentifiableServiceImpl<FileResource>
    implements FileResourceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourceServiceImpl.class);

  private final FileResourceBinaryRepository binaryRepository;

  @Autowired
  public FileResourceServiceImpl(
      FileResourceMetadataRepository metadataRepository,
      FileResourceBinaryRepository binaryRepository) {
    super(metadataRepository);
    this.binaryRepository = binaryRepository;
  }

  @Override
  public FileResource createByMimeType(MimeType mimeType) {
    return ((FileResourceMetadataRepository) repository).createByMimeType(mimeType);
  }

  @Override
  public FileResource get(UUID uuid) {
    return repository.findOne(uuid);
  }

  @Override
  public FileResource getByIdentifier(String namespace, String id)
      throws IdentifiableServiceException {
    Identifier identifier = new IdentifierImpl(null, namespace, id);
    return repository.findOne(identifier);
  }

  @Override
  public FileResource save(FileResource fileResource, InputStream binaryData)
      throws IdentifiableServiceException {
    try {
      fileResource = binaryRepository.save(fileResource, binaryData);
      fileResource = repository.save(fileResource);
      return fileResource;
    } catch (ResourceIOException e) {
      LOGGER.error("Cannot save fileResource " + fileResource.getFilename() + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
