package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ApplicationFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.AudioFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ImageFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.LinkedDataFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.TextFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.VideoFileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.identifiable.IdentifiableServiceImpl;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import de.digitalcollections.model.identifiable.resource.VideoFileResource;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service("fileResourceMetadataService")
public class FileResourceMetadataServiceImpl
    extends IdentifiableServiceImpl<FileResource, FileResourceMetadataRepository<FileResource>>
    implements FileResourceMetadataService<FileResource> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataServiceImpl.class);

  protected final ApplicationFileResourceService applicationFileResourceService;
  protected final AudioFileResourceService audioFileResourceService;
  protected final ImageFileResourceService imageFileResourceService;
  protected final LinkedDataFileResourceService linkedDataFileResourceService;
  private final LocaleService localeService;
  protected final TextFileResourceService textFileResourceService;
  protected final VideoFileResourceService videoFileResourceService;

  public FileResourceMetadataServiceImpl(
      FileResourceMetadataRepository<FileResource> metadataRepository,
      ApplicationFileResourceService applicationFileResourceService,
      AudioFileResourceService audioFileResourceService,
      ImageFileResourceService imageFileResourceService,
      LinkedDataFileResourceService linkedDataFileResourceService,
      TextFileResourceService textFileResourceService,
      VideoFileResourceService videoFileResourceService,
      LocaleService localeService,
      IdentifierService identifierService,
      UrlAliasService urlAliasService,
      CudamiConfig cudamiConfig) {
    super(metadataRepository, identifierService, urlAliasService, localeService, cudamiConfig);
    this.applicationFileResourceService = applicationFileResourceService;
    this.audioFileResourceService = audioFileResourceService;
    this.imageFileResourceService = imageFileResourceService;
    this.linkedDataFileResourceService = linkedDataFileResourceService;
    this.textFileResourceService = textFileResourceService;
    this.videoFileResourceService = videoFileResourceService;
    this.localeService = localeService;
  }

  @Override
  public FileResource createByMimeType(MimeType mimeType) {
    return ((FileResourceMetadataRepository) repository).createByMimeType(mimeType);
  }

  @Override
  public FileResource getByExample(FileResource example) throws ServiceException {
    FileResource fileResource;
    try {
      fileResource = repository.getByExample(example);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return getTypeSpecific(fileResource);
  }

  @Override
  public FileResource getByIdentifier(Identifier identifier) throws ServiceException {
    FileResource fileResource;
    try {
      fileResource = repository.getByIdentifier(identifier);
    } catch (RepositoryException e) {
      throw new ServiceException("Backend failure", e);
    }
    return getTypeSpecific(fileResource);
  }

  private FileResource getTypeSpecific(FileResource fileResource) {
    if (fileResource == null) {
      return null;
    }
    FileResource specificFileResource = createByMimeType(fileResource.getMimeType());
    try {
      switch (specificFileResource.getIdentifiableObjectType()) {
        case APPLICATION_FILE_RESOURCE:
          ApplicationFileResource applicationFileResource = applicationFileResourceService.create();
          BeanUtils.copyProperties(fileResource, applicationFileResource);
          return applicationFileResourceService.getByExample(applicationFileResource);
        case AUDIO_FILE_RESOURCE:
          AudioFileResource audioFileResource = audioFileResourceService.create();
          BeanUtils.copyProperties(fileResource, audioFileResource);
          return audioFileResourceService.getByExample(audioFileResource);
        case IMAGE_FILE_RESOURCE:
          ImageFileResource imageFileResource = imageFileResourceService.create();
          BeanUtils.copyProperties(fileResource, imageFileResource);
          return imageFileResourceService.getByExample(imageFileResource);
        case LINKED_DATA_FILE_RESOURCE:
          LinkedDataFileResource linkedDataFileResource = linkedDataFileResourceService.create();
          BeanUtils.copyProperties(fileResource, linkedDataFileResource);
          return linkedDataFileResourceService.getByExample(linkedDataFileResource);
        case TEXT_FILE_RESOURCE:
          TextFileResource textFileResource = textFileResourceService.create();
          BeanUtils.copyProperties(fileResource, textFileResource);
          return textFileResourceService.getByExample(textFileResource);
        case VIDEO_FILE_RESOURCE:
          VideoFileResource videoFileResource = videoFileResourceService.create();
          BeanUtils.copyProperties(fileResource, videoFileResource);
          return videoFileResourceService.getByExample(videoFileResource);
        default:
          return fileResource;
      }
    } catch (ServiceException ex) {
      LOGGER.error(
          "Cannot get type specific data for fileresource. Returning generic fileresource.", ex);
    }
    return fileResource;
  }

  @Override
  public void save(FileResource fileResource) throws ServiceException, ValidationException {
    if (fileResource.getLabel() == null && fileResource.getFilename() != null) {
      // set a default label = filename (an empty label violates constraint)
      fileResource.setLabel(
          new LocalizedText(
              new Locale(localeService.getDefaultLanguage()), fileResource.getFilename()));
    }
    if (fileResource instanceof ApplicationFileResource) {
      applicationFileResourceService.save((ApplicationFileResource) fileResource);
    } else if (fileResource instanceof AudioFileResource) {
      audioFileResourceService.save((AudioFileResource) fileResource);
    } else if (fileResource instanceof ImageFileResource) {
      imageFileResourceService.save((ImageFileResource) fileResource);
    } else if (fileResource instanceof LinkedDataFileResource) {
      linkedDataFileResourceService.save((LinkedDataFileResource) fileResource);
    } else if (fileResource instanceof TextFileResource) {
      textFileResourceService.save((TextFileResource) fileResource);
    } else if (fileResource instanceof VideoFileResource) {
      videoFileResourceService.save((VideoFileResource) fileResource);
    } else {
      super.save(fileResource);
    }
  }

  @Override
  public void update(FileResource fileResource) throws ServiceException, ValidationException {
    if (fileResource instanceof ApplicationFileResource) {
      applicationFileResourceService.update((ApplicationFileResource) fileResource);
    } else if (fileResource instanceof AudioFileResource) {
      audioFileResourceService.update((AudioFileResource) fileResource);
    } else if (fileResource instanceof ImageFileResource) {
      imageFileResourceService.update((ImageFileResource) fileResource);
    } else if (fileResource instanceof LinkedDataFileResource) {
      linkedDataFileResourceService.update((LinkedDataFileResource) fileResource);
    } else if (fileResource instanceof TextFileResource) {
      textFileResourceService.update((TextFileResource) fileResource);
    } else if (fileResource instanceof VideoFileResource) {
      videoFileResourceService.update((VideoFileResource) fileResource);
    } else {
      super.update(fileResource);
    }
  }
}
