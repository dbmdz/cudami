package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ConflictException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
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
import de.digitalcollections.model.validation.ValidationException;
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
  public boolean delete(FileResource fileResource) throws ConflictException, ServiceException {
    switch (fileResource.getFileResourceType()) {
      case APPLICATION:
        ApplicationFileResource applicationFileResource = applicationFileResourceService.create();
        BeanUtils.copyProperties(fileResource, applicationFileResource);
        return applicationFileResourceService.delete(applicationFileResource);
      case AUDIO:
        AudioFileResource audioFileResource = audioFileResourceService.create();
        BeanUtils.copyProperties(fileResource, audioFileResource);
        return audioFileResourceService.delete(audioFileResource);
      case IMAGE:
        ImageFileResource imageFileResource = imageFileResourceService.create();
        BeanUtils.copyProperties(fileResource, imageFileResource);
        return imageFileResourceService.delete(imageFileResource);
      case LINKED_DATA:
        LinkedDataFileResource linkedDataFileResource = linkedDataFileResourceService.create();
        BeanUtils.copyProperties(fileResource, linkedDataFileResource);
        return linkedDataFileResourceService.delete(linkedDataFileResource);
      case TEXT:
        TextFileResource textFileResource = textFileResourceService.create();
        BeanUtils.copyProperties(fileResource, textFileResource);
        return textFileResourceService.delete(textFileResource);
      case VIDEO:
        VideoFileResource videoFileResource = videoFileResourceService.create();
        BeanUtils.copyProperties(fileResource, videoFileResource);
        return videoFileResourceService.delete(videoFileResource);
      default:
        return super.delete(fileResource);
    }
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
      switch (specificFileResource.getFileResourceType()) {
        case APPLICATION:
          ApplicationFileResource applicationFileResource = applicationFileResourceService.create();
          BeanUtils.copyProperties(fileResource, applicationFileResource);
          return applicationFileResourceService.getByExample(applicationFileResource);
        case AUDIO:
          AudioFileResource audioFileResource = audioFileResourceService.create();
          BeanUtils.copyProperties(fileResource, audioFileResource);
          return audioFileResourceService.getByExample(audioFileResource);
        case IMAGE:
          ImageFileResource imageFileResource = imageFileResourceService.create();
          BeanUtils.copyProperties(fileResource, imageFileResource);
          return imageFileResourceService.getByExample(imageFileResource);
        case LINKED_DATA:
          LinkedDataFileResource linkedDataFileResource = linkedDataFileResourceService.create();
          BeanUtils.copyProperties(fileResource, linkedDataFileResource);
          return linkedDataFileResourceService.getByExample(linkedDataFileResource);
        case TEXT:
          TextFileResource textFileResource = textFileResourceService.create();
          BeanUtils.copyProperties(fileResource, textFileResource);
          return textFileResourceService.getByExample(textFileResource);
        case VIDEO:
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
    switch (fileResource.getFileResourceType()) {
      case APPLICATION:
        ApplicationFileResource applicationFileResource = applicationFileResourceService.create();
        BeanUtils.copyProperties(fileResource, applicationFileResource);
        applicationFileResourceService.save(applicationFileResource);
        BeanUtils.copyProperties(applicationFileResource, fileResource);
        break;
      case AUDIO:
        AudioFileResource audioFileResource = audioFileResourceService.create();
        BeanUtils.copyProperties(fileResource, audioFileResource);
        audioFileResourceService.save(audioFileResource);
        BeanUtils.copyProperties(audioFileResource, fileResource);
        break;
      case IMAGE:
        ImageFileResource imageFileResource = imageFileResourceService.create();
        BeanUtils.copyProperties(fileResource, imageFileResource);
        imageFileResourceService.save(imageFileResource);
        BeanUtils.copyProperties(imageFileResource, fileResource);
        break;
      case TEXT:
        TextFileResource textFileResource = textFileResourceService.create();
        BeanUtils.copyProperties(fileResource, textFileResource);
        textFileResourceService.save(textFileResource);
        BeanUtils.copyProperties(textFileResource, fileResource);
        break;
      case VIDEO:
        VideoFileResource videoFileResource = videoFileResourceService.create();
        BeanUtils.copyProperties(fileResource, videoFileResource);
        videoFileResourceService.save(videoFileResource);
        BeanUtils.copyProperties(videoFileResource, fileResource);
        break;
      default:
        super.save(fileResource);
    }
  }

  @Override
  public void update(FileResource fileResource) throws ServiceException, ValidationException {
    switch (fileResource.getFileResourceType()) {
      case APPLICATION:
        ApplicationFileResource applicationFileResource = applicationFileResourceService.create();
        BeanUtils.copyProperties(fileResource, applicationFileResource);
        applicationFileResourceService.update(applicationFileResource);
        BeanUtils.copyProperties(applicationFileResource, fileResource);
        break;
      case AUDIO:
        AudioFileResource audioFileResource = audioFileResourceService.create();
        BeanUtils.copyProperties(fileResource, audioFileResource);
        audioFileResourceService.update(audioFileResource);
        BeanUtils.copyProperties(audioFileResource, fileResource);
        break;
      case IMAGE:
        ImageFileResource imageFileResource = imageFileResourceService.create();
        BeanUtils.copyProperties(fileResource, imageFileResource);
        imageFileResourceService.update(imageFileResource);
        BeanUtils.copyProperties(imageFileResource, fileResource);
        break;
      case TEXT:
        TextFileResource textFileResource = textFileResourceService.create();
        BeanUtils.copyProperties(fileResource, textFileResource);
        textFileResourceService.update(textFileResource);
        BeanUtils.copyProperties(textFileResource, fileResource);
        break;
      case VIDEO:
        VideoFileResource videoFileResource = videoFileResourceService.create();
        BeanUtils.copyProperties(fileResource, videoFileResource);
        videoFileResourceService.update(videoFileResource);
        BeanUtils.copyProperties(videoFileResource, fileResource);
        break;
      default:
        super.update(fileResource);
    }
  }
}
