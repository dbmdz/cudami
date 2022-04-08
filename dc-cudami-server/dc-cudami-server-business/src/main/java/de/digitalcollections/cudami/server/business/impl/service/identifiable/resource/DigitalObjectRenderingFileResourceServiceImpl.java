package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectRenderingFileResourceRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ApplicationFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.AudioFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ImageFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.TextFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.VideoFileResourceService;
import de.digitalcollections.model.identifiable.resource.FileResource;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DigitalObjectRenderingFileResourceServiceImpl extends FileResourceMetadataServiceImpl
    implements DigitalObjectRenderingFileResourceService {

  private final DigitalObjectRenderingFileResourceRepository renderingFileResourceRepository;

  public DigitalObjectRenderingFileResourceServiceImpl(
      @Qualifier("fileResourceMetadataRepositoryImpl")
          FileResourceMetadataRepository<FileResource> metadataRepository,
      @Qualifier("applicationFileResourceServiceImpl")
          ApplicationFileResourceService applicationFileResourceService,
      @Qualifier("audioFileResourceServiceImpl") AudioFileResourceService audioFileResourceService,
      @Qualifier("imageFileResourceServiceImpl") ImageFileResourceService imageFileResourceService,
      @Qualifier("linkedDataFileResourceServiceImpl")
          LinkedDataFileResourceService linkedDataFileResourceService,
      @Qualifier("textFileResourceServiceImpl") TextFileResourceService textFileResourceService,
      @Qualifier("videoFileResourceServiceImpl") VideoFileResourceService videoFileResourceService,
      DigitalObjectRenderingFileResourceRepository renderingFileResourceRepository,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService,
      LocaleService localeService,
      CudamiConfig cudamiConfig) {
    super(
        metadataRepository,
        applicationFileResourceService,
        audioFileResourceService,
        imageFileResourceService,
        linkedDataFileResourceService,
        textFileResourceService,
        videoFileResourceService,
        localeService,
        identifierRepository,
        urlAliasService,
        cudamiConfig);
    this.renderingFileResourceRepository = renderingFileResourceRepository;
  }

  @Override
  public List<FileResource> saveForDigitalObject(
      UUID digitalObjectUuid, List<FileResource> renderingResources) {
    return renderingFileResourceRepository.saveForDigitalObjectUuid(
        digitalObjectUuid, renderingResources);
  }

  @Override
  public List<FileResource> getForDigitalObject(UUID digitalObjectUuid) {
    return renderingFileResourceRepository.getForDigitalObjectUuid(digitalObjectUuid);
  }
}
