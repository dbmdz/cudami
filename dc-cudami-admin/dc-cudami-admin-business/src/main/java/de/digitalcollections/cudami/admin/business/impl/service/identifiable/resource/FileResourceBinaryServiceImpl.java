package de.digitalcollections.cudami.admin.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.admin.backend.api.repository.identifiable.resource.FileResourceBinaryRepository;
import de.digitalcollections.cudami.admin.business.api.service.identifiable.resource.FileResourceBinaryService;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.exceptions.ResourceIOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
// @Transactional(readOnly = true)
public class FileResourceBinaryServiceImpl implements FileResourceBinaryService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileResourceBinaryServiceImpl.class);

  private final FileResourceBinaryRepository repository;

  @Autowired
  public FileResourceBinaryServiceImpl(FileResourceBinaryRepository repository) {
    this.repository = repository;
  }

  @Override
  public FileResource upload(InputStream inputStream, String filename, String contentType)
      throws ResourceIOException {
    return repository.upload(inputStream, filename, contentType);
  }

  @Override
  public FileResource upload(byte[] bytes, String filename, String contentType)
      throws ResourceIOException {
    return repository.upload(bytes, filename, contentType);
  }
}
