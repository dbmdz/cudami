package de.digitalcollections.cudami.server.business.impl.service.identifiable.resource;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.resource.DigitalObjectRenderingFileResourceRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.CudamiServiceException;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.DigitalObjectRenderingFileResourceService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.identifiable.resource.TextFileResource;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The DigitalObjectRenderingFileResourceService")
class DigitalObjectRenderingFileResourceServiceImplTest {

  private DigitalObjectRenderingFileResourceRepository repo;
  private DigitalObjectRenderingFileResourceService service;

  private ApplicationFileResourceServiceImpl applicationFileResourceService;
  private AudioFileResourceServiceImpl audioFileResourceService;
  private FileResourceMetadataService<FileResource> fileResourceMetadataService;
  private ImageFileResourceServiceImpl imageFileResourceService;
  private LinkedDataFileResourceServiceImpl linkedDataFileResourceService;
  private TextFileResourceServiceImpl textFileResourceService;
  private VideoFileResourceServiceImpl videoFileResourceService;

  @BeforeEach
  public void beforeEach() throws CudamiServiceException {
    applicationFileResourceService = mock(ApplicationFileResourceServiceImpl.class);
    audioFileResourceService = mock(AudioFileResourceServiceImpl.class);
    fileResourceMetadataService = mock(FileResourceMetadataService.class);
    imageFileResourceService = mock(ImageFileResourceServiceImpl.class);
    linkedDataFileResourceService = mock(LinkedDataFileResourceServiceImpl.class);
    textFileResourceService = mock(TextFileResourceServiceImpl.class);
    repo = mock(DigitalObjectRenderingFileResourceRepository.class);
    videoFileResourceService = mock(VideoFileResourceServiceImpl.class);
    service =
        new DigitalObjectRenderingFileResourceServiceImpl(
            applicationFileResourceService,
            audioFileResourceService,
            fileResourceMetadataService,
            imageFileResourceService,
            linkedDataFileResourceService,
            textFileResourceService,
            videoFileResourceService,
            repo);
  }

  @DisplayName("can delete resource and relation")
  @Test
  public void deleteResourceAndRelation()
      throws CudamiServiceException, IdentifiableServiceException {
    UUID uuid = UUID.randomUUID();
    DigitalObject digitalObject = DigitalObject.builder().uuid(uuid).label("Label").build();
    TextFileResource renderingFileResource =
        TextFileResource.builder().mimeType(MimeType.fromTypename("text/html")).build();

    digitalObject.setRenderingResources(List.of(renderingFileResource));

    when(repo.getRenderingFileResources(eq(uuid))).thenReturn(List.of(renderingFileResource));

    service.deleteRenderingFileResources(uuid);

    verify(repo, times(1)).delete(renderingFileResource.getUuid());
    verify(textFileResourceService, times(1)).delete(renderingFileResource.getUuid());
  }
}
