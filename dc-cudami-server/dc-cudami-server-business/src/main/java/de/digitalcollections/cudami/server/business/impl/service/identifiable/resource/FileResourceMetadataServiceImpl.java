package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileResourceMetadataServiceImpl extends IdentifiableServiceImpl<FileResource>
    implements FileResourceMetadataService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataServiceImpl.class);

  @Autowired
  public FileResourceMetadataServiceImpl(FileResourceMetadataRepository metadataRepository) {
    super(metadataRepository);
  }

  @Override
  public FileResource createByMimeType(MimeType mimeType) {
    return ((FileResourceMetadataRepository) repository).createByMimeType(mimeType);
  }

  @Override
  public FileResource get(UUID uuid) {
    return repository.findOne(uuid);
  }
}
