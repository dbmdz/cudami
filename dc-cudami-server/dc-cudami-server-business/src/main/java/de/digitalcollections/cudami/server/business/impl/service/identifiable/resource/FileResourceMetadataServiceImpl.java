package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.IdentifierRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.FileResourceMetadataRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ValidationException;
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
import de.digitalcollections.model.identifiable.resource.ApplicationFileResource;
import de.digitalcollections.model.identifiable.resource.AudioFileResource;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.identifiable.resource.LinkedDataFileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import de.digitalcollections.model.identifiable.resource.VideoFileResource;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

// @Transactional should not be set in derived class to prevent overriding, check base class instead
@Service("fileResourceMetadataService")
public class FileResourceMetadataServiceImpl extends IdentifiableServiceImpl<FileResource>
    implements FileResourceMetadataService<FileResource> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(FileResourceMetadataServiceImpl.class);

  private final ApplicationFileResourceService applicationFileResourceService;
  private final AudioFileResourceService audioFileResourceService;
  private final ImageFileResourceService imageFileResourceService;
  private final LinkedDataFileResourceService linkedDataFileResourceService;
  private final LocaleService localeService;
  private final TextFileResourceService textFileResourceService;
  private final VideoFileResourceService videoFileResourceService;

  @Autowired
  public FileResourceMetadataServiceImpl(
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
      LocaleService localeService,
      IdentifierRepository identifierRepository,
      UrlAliasService urlAliasService,
      CudamiConfig cudamiConfig) {
    super(metadataRepository, identifierRepository, urlAliasService, cudamiConfig);
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
  public FileResource get(UUID uuid) {
    FileResource fileResource = repository.findOne(uuid);
    return getTypeSpecific(fileResource);
  }

  @Override
  public FileResource getByIdentifier(String namespace, String id) {
    FileResource fileResource = repository.findOneByIdentifier(namespace, id);
    return getTypeSpecific(fileResource);
  }

  private FileResource getTypeSpecific(FileResource fileResource) {
    if (fileResource == null) {
      return null;
    }
    FileResource specificFileResource = createByMimeType(fileResource.getMimeType());
    if (specificFileResource instanceof ApplicationFileResource) {
      return applicationFileResourceService.get(fileResource.getUuid());
    } else if (specificFileResource instanceof AudioFileResource) {
      return audioFileResourceService.get(fileResource.getUuid());
    } else if (specificFileResource instanceof ImageFileResource) {
      return imageFileResourceService.get(fileResource.getUuid());
    } else if (specificFileResource instanceof LinkedDataFileResource) {
      return linkedDataFileResourceService.get(fileResource.getUuid());
    } else if (specificFileResource instanceof TextFileResource) {
      return textFileResourceService.get(fileResource.getUuid());
    } else if (specificFileResource instanceof VideoFileResource) {
      return videoFileResourceService.get(fileResource.getUuid());
    }
    return fileResource;
  }

  @Override
  public FileResource save(FileResource fileResource)
      throws IdentifiableServiceException, ValidationException {
    if (fileResource.getLabel() == null && fileResource.getFilename() != null) {
      // set a default label = filename (an empty label violates constraint)
      fileResource.setLabel(
          new LocalizedText(
              new Locale(localeService.getDefaultLanguage()), fileResource.getFilename()));
    }
    if (fileResource instanceof ApplicationFileResource) {
      return applicationFileResourceService.save((ApplicationFileResource) fileResource);
    } else if (fileResource instanceof AudioFileResource) {
      return audioFileResourceService.save((AudioFileResource) fileResource);
    } else if (fileResource instanceof ImageFileResource) {
      return imageFileResourceService.save((ImageFileResource) fileResource);
    } else if (fileResource instanceof LinkedDataFileResource) {
      return linkedDataFileResourceService.save((LinkedDataFileResource) fileResource);
    } else if (fileResource instanceof TextFileResource) {
      return textFileResourceService.save((TextFileResource) fileResource);
    } else if (fileResource instanceof VideoFileResource) {
      return videoFileResourceService.save((VideoFileResource) fileResource);
    }
    return super.save(fileResource);
  }

  @Override
  public FileResource update(FileResource fileResource)
      throws IdentifiableServiceException, ValidationException {
    if (fileResource instanceof ApplicationFileResource) {
      return applicationFileResourceService.update((ApplicationFileResource) fileResource);
    } else if (fileResource instanceof AudioFileResource) {
      return audioFileResourceService.update((AudioFileResource) fileResource);
    } else if (fileResource instanceof ImageFileResource) {
      return imageFileResourceService.update((ImageFileResource) fileResource);
    } else if (fileResource instanceof LinkedDataFileResource) {
      return linkedDataFileResourceService.update((LinkedDataFileResource) fileResource);
    } else if (fileResource instanceof TextFileResource) {
      return textFileResourceService.update((TextFileResource) fileResource);
    } else if (fileResource instanceof VideoFileResource) {
      return videoFileResourceService.update((VideoFileResource) fileResource);
    }
    return super.update(fileResource);
  }
}
