package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.ImageFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ImageFileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageFileResourceServiceImpl extends IdentifiableServiceImpl<ImageFileResource>
    implements ImageFileResourceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ImageFileResourceServiceImpl.class);

  @Autowired
  public ImageFileResourceServiceImpl(ImageFileResourceRepository imageFileResourceRepository) {
    super(imageFileResourceRepository);
  }

  @Override
  public FileResource createByMimeType(MimeType mimeType) {
    return ((FileResourceMetadataRepository) repository).createByMimeType(mimeType);
  }
}
