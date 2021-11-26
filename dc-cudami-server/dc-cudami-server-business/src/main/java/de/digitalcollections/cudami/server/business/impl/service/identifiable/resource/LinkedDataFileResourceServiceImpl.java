package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.LinkedDataFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LinkedDataFileResourceServiceImpl
    extends IdentifiableServiceImpl<LinkedDataFileResource>
    implements LinkedDataFileResourceService {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(LinkedDataFileResourceServiceImpl.class);

  @Autowired
  public LinkedDataFileResourceServiceImpl(
      LinkedDataFileResourceRepository linkedDataFileResourceRepository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService) {
    super(linkedDataFileResourceRepository, identifierRepository, urlAliasService);
  }

  @Override
  public FileResource createByMimeType(MimeType mimeType) {
    return ((FileResourceMetadataRepository) repository).createByMimeType(mimeType);
  }
}
