package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.LinkedDataFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.api.identifiable.resource.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LinkedDataFileResourceServiceImpl
    extends IdentifiableServiceImpl<LinkedDataFileResource>
    implements LinkedDataFileResourceService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LinkedDataFileResourceServiceImpl.class);

  @Autowired
  public LinkedDataFileResourceServiceImpl(
      LinkedDataFileResourceRepository linkedDataFileResourceRepository) {
    super(linkedDataFileResourceRepository);
  }

  @Override
  public FileResource createByMimeType(MimeType mimeType) {
    return ((FileResourceMetadataRepository) repository).createByMimeType(mimeType);
  }
}
