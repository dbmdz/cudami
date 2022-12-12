package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectRenderingFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ApplicationFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.AudioFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
/**
 * This service persists all rendering resources (which can be of any type, derived from
 * FileResource) of a DigitalObject
 */
public class DigitalObjectRenderingFileResourceServiceImpl
    implements DigitalObjectRenderingFileResourceService {

  protected final ApplicationFileResourceService applicationFileResourceService;
  protected final AudioFileResourceService audioFileResourceService;
  private final DigitalObjectRenderingFileResourceRepository
      digitalObjectRenderingFileResourceRepository;
  protected final FileResourceMetadataService<FileResource> fileResourceMetadataService;
  protected final ImageFileResourceService imageFileResourceService;
  protected final LinkedDataFileResourceService linkedDataFileResourceService;
  protected final TextFileResourceService textFileResourceService;
  protected final VideoFileResourceService videoFileResourceService;

  public DigitalObjectRenderingFileResourceServiceImpl(
      ApplicationFileResourceService applicationFileResourceService,
      AudioFileResourceService audioFileResourceService,
      @Qualifier("fileResourceMetadataService")
          FileResourceMetadataService<FileResource> fileResourceMetadataService,
      ImageFileResourceService imageFileResourceService,
      LinkedDataFileResourceService linkedDataFileResourceService,
      TextFileResourceService textFileResourceService,
      VideoFileResourceService videoFileResourceService,
      DigitalObjectRenderingFileResourceRepository digitalObjectRenderingFileResourceRepository) {

    this.applicationFileResourceService = applicationFileResourceService;
    this.audioFileResourceService = audioFileResourceService;
    this.imageFileResourceService = imageFileResourceService;
    this.fileResourceMetadataService = fileResourceMetadataService;
    this.linkedDataFileResourceService = linkedDataFileResourceService;
    this.textFileResourceService = textFileResourceService;
    this.videoFileResourceService = videoFileResourceService;
    this.digitalObjectRenderingFileResourceRepository =
        digitalObjectRenderingFileResourceRepository;
  }

  private boolean deleteRenderingResource(FileResource renderingResource)
      throws IdentifiableServiceException, ConflictException {
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
        return fileResourceMetadataService.delete(renderingResource.getUuid());
    }
  }

  @Override
  public List<FileResource> getRenderingFileResources(UUID digitalObjectUuid) {
    return digitalObjectRenderingFileResourceRepository.getRenderingFileResources(
        digitalObjectUuid);
  }

  private void saveRenderingFileResource(FileResource renderingResource)
      throws ValidationException, ServiceException {
    switch (renderingResource.getMimeType().getPrimaryType()) {
      case "application":
        applicationFileResourceService.save((ApplicationFileResource) renderingResource);
      case "audio":
        audioFileResourceService.save((AudioFileResource) renderingResource);
      case "image":
        imageFileResourceService.save((ImageFileResource) renderingResource);
      case "text":
        textFileResourceService.save((TextFileResource) renderingResource);
      case "video":
        videoFileResourceService.save((VideoFileResource) renderingResource);
      default:
        fileResourceMetadataService.save(renderingResource);
    }
  }

  @Override
  public List<FileResource> setRenderingFileResources(
      UUID digitalObjectUuid, List<FileResource> renderingResources) throws CudamiServiceException {

    // Remove the old rendering resources, if present
    List<FileResource> existingRenderingResources = getRenderingFileResources(digitalObjectUuid);
    for (FileResource existingRenderingResource : existingRenderingResources) {
      try {
        deleteRenderingResource(existingRenderingResource);
      } catch (ConflictException | IdentifiableServiceException e) {
        throw new CudamiServiceException(
            "Cannot remove existing rendering resource=" + existingRenderingResource + ": " + e, e);
      }
    }

    // Remove the old relations
    digitalObjectRenderingFileResourceRepository.removeByDigitalObject(digitalObjectUuid);

    // Persist the new rendering resources
    if (renderingResources != null) {
      // first save rendering resources
      List<FileResource> savedRenderingResources = new ArrayList<>();
      for (FileResource renderingResource : renderingResources) {
        try {
          saveRenderingFileResource(renderingResource);
        } catch (ValidationException | ServiceException e) {
          throw new CudamiServiceException(
              "Cannot save RenderingResource" + renderingResource + ": " + e, e);
        }
        savedRenderingResources.add(renderingResource);
      }

      // Persist the new relations
      digitalObjectRenderingFileResourceRepository.saveRenderingFileResources(
          digitalObjectUuid, savedRenderingResources);
    }

    return renderingResources;
  }

  @Override
  public void deleteRenderingFileResources(UUID digitalObjectUuid) throws CudamiServiceException {
    List<FileResource> renderingFileResources = getRenderingFileResources(digitalObjectUuid);
    if (renderingFileResources == null || renderingFileResources.isEmpty()) {
      return;
    }

    for (FileResource renderingFileResource : renderingFileResources) {
      try {
        // Delete the relation
        int amountDeletedRelations =
            digitalObjectRenderingFileResourceRepository.delete(renderingFileResource.getUuid());
        if (amountDeletedRelations != 1) {
          throw new CudamiServiceException(
              "Could not delete relation for RenderingFileResource="
                  + renderingFileResource
                  + " for DigitalObject with uuid="
                  + digitalObjectUuid);
        }

        // Delete the resource, when no references exist to it
        if (digitalObjectRenderingFileResourceRepository.countDigitalObjectsForResource(
                renderingFileResource.getUuid())
            == 0) {
          deleteRenderingResource(renderingFileResource);
        }
      } catch (ConflictException | IdentifiableServiceException e) {
        throw new CudamiServiceException(
            "Cannot delete RenderingFileResource="
                + renderingFileResource
                + " for DigitalObject with uuid="
                + digitalObjectUuid
                + ": "
                + e,
            e);
      }
    }
  }
}
