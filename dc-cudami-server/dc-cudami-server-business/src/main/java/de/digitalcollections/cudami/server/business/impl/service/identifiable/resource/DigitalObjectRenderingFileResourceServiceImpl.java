package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectRenderingFileResourceRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ApplicationFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.AudioFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ImageFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.TextFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.VideoFileResourceService;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import de.digitalcollections.model.identifiable.resource.VideoFileResource;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
/**
 * This service persists all rendering resources (which can be of any type, derived from
 * FileResource) of a DigitalObject
 */
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

    // Remove the old rendering resources, if present
    List<FileResource> existingRenderingResources = getForDigitalObject(digitalObjectUuid);
    existingRenderingResources.forEach(
        r -> {
          try {
            deleteRenderingResource(r);
          } catch (IdentifiableServiceException e) {
            throw new RuntimeException("Cannot save rendering resource=" + r + ": " + e, e);
          }
        });

    // Remove the old relations
    renderingFileResourceRepository.removeByDigitalObject(digitalObjectUuid);

    // Persist the new rendering resources
    if (renderingResources != null) {
      // first save rendering resources
      renderingResources =
          renderingResources.stream()
              .map(
                  r -> {
                    try {
                      return saveRenderingResource(r);
                    } catch (Exception e) {
                      throw new RuntimeException(
                          "Cannot save rendering resource=" + r + ": " + e, e);
                    }
                  })
              .collect(Collectors.toList());

      // Persist the new relations
      renderingFileResourceRepository.saveForDigitalObject(digitalObjectUuid, renderingResources);
    }

    return renderingResources;
  }

  private boolean deleteRenderingResource(FileResource renderingResource)
      throws IdentifiableServiceException {
    switch (renderingResource.getMimeType().getPrimaryType()) {
      case "application":
        return applicationFileResourceService.delete(renderingResource.getUuid());
      case "audio":
        return audioFileResourceService.delete(renderingResource.getUuid());
      case "image":
        return imageFileResourceService.delete(renderingResource.getUuid());
      case "text":
        return textFileResourceService.delete(renderingResource.getUuid());
      case "video":
        return videoFileResourceService.delete(renderingResource.getUuid());
      default:
        return delete(renderingResource.getUuid());
    }
  }

  private FileResource saveRenderingResource(FileResource renderingResource)
      throws ValidationException, IdentifiableServiceException {
    if (renderingResource.getUuid() == null) {
      switch (renderingResource.getMimeType().getPrimaryType()) {
        case "application":
          return applicationFileResourceService.save((ApplicationFileResource) renderingResource);
        case "audio":
          return audioFileResourceService.save((AudioFileResource) renderingResource);
        case "image":
          return imageFileResourceService.save((ImageFileResource) renderingResource);
        case "text":
          return textFileResourceService.save((TextFileResource) renderingResource);
        case "video":
          return videoFileResourceService.save((VideoFileResource) renderingResource);
        default:
          return save(renderingResource);
      }
    }

    return renderingResource;
  }

  @Override
  public List<FileResource> getForDigitalObject(UUID digitalObjectUuid) {
    return renderingFileResourceRepository.findByDigitalObject(digitalObjectUuid);
  }
}
