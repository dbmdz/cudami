package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceBinaryRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceBinaryService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.model.exception.ResourceIOException;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class FileResourceBinaryServiceImpl implements FileResourceBinaryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourceBinaryServiceImpl.class);

  private final FileResourceBinaryRepository binaryRepository;
  private final FileResourceMetadataService<FileResource> metadataService;

  public FileResourceBinaryServiceImpl(
      FileResourceBinaryRepository binaryRepository,
      @Qualifier("fileResourceMetadataService")
          FileResourceMetadataService<FileResource> metadataService) {
    this.binaryRepository = binaryRepository;
    this.metadataService = metadataService;
  }

  @Override
  public void assertReadability(FileResource resource) throws IdentifiableServiceException {
    try {
      binaryRepository.assertReadability(resource);
    } catch (ResourceIOException | ResourceNotFoundException ex) {
      throw new IdentifiableServiceException(
          "File resource " + resource.getUri() + " not readable.", ex);
    }
  }

  @Override
  public FileResource find(String uuid, MimeType mimeType) throws IdentifiableServiceException {
    try {
      return binaryRepository.find(uuid, mimeType);
    } catch (ResourceIOException | ResourceNotFoundException ex) {
      throw new IdentifiableServiceException("File resource " + uuid + " not found.", ex);
    }
  }

  @Override
  public byte[] getAsBytes(FileResource resource) throws IdentifiableServiceException {
    try {
      return binaryRepository.getAsBytes(resource);
    } catch (ResourceIOException | ResourceNotFoundException ex) {
      throw new IdentifiableServiceException(
          "Can not return file resource " + resource.getUri() + " as bytes.", ex);
    }
  }

  @Override
  public Document getAsDocument(FileResource resource) throws IdentifiableServiceException {
    try {
      return binaryRepository.getAsDocument(resource);
    } catch (ResourceIOException | ResourceNotFoundException ex) {
      throw new IdentifiableServiceException(
          "Can not return file resource " + resource.getUri() + " as document.", ex);
    }
  }

  @Override
  public InputStream getInputStream(FileResource resource) throws IdentifiableServiceException {
    try {
      return binaryRepository.getInputStream(resource);
    } catch (ResourceIOException | ResourceNotFoundException ex) {
      throw new IdentifiableServiceException(
          "Can not return file resource " + resource.getUri() + " as inputstream.", ex);
    }
  }

  @Override
  public FileResource save(FileResource fileResource, InputStream binaryData)
      throws IdentifiableServiceException, ValidationException {
    try {
      fileResource = binaryRepository.save(fileResource, binaryData);
      fileResource = metadataService.save(fileResource);
      return fileResource;
    } catch (ResourceIOException e) {
      LOGGER.error("Cannot save fileResource " + fileResource.getFilename() + ": ", e);
      throw new IdentifiableServiceException(e.getMessage());
    }
  }
}
