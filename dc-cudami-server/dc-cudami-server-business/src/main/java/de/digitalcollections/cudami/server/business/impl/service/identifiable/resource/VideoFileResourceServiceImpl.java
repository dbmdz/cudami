package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.VideoFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.VideoFileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import de.digitalcollections.model.api.identifiable.resource.VideoFileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoFileResourceServiceImpl extends IdentifiableServiceImpl<VideoFileResource>
    implements VideoFileResourceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(VideoFileResourceServiceImpl.class);

  @Autowired
  public VideoFileResourceServiceImpl(VideoFileResourceRepository videoFileResourceRepository) {
    super(videoFileResourceRepository);
  }

  @Override
  public FileResource createByMimeType(MimeType mimeType) {
    return ((FileResourceMetadataRepository) repository).createByMimeType(mimeType);
  }
}
