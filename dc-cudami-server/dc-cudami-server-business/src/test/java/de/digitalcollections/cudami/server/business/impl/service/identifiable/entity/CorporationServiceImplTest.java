package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.CorporationRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.ExternalCorporationRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.model.api.identifiable.entity.Corporation;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.impl.identifiable.entity.CorporationImpl;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The corporation service")
class CorporationServiceImplTest {

  private CorporationServiceImpl corporationService;
  private CorporationRepository corporationRepository;
  private ExternalCorporationRepository externalCorporationRepository;
  private FileResourceMetadataService fileResourceMetadataService;

  @BeforeEach
  void setUp() throws IdentifiableServiceException {
    corporationRepository = mock(CorporationRepository.class);
    when(corporationRepository.save(any(Corporation.class))).thenReturn(new CorporationImpl());
    when(corporationRepository.save(eq(null))).thenThrow(new NullPointerException());

    externalCorporationRepository = mock(ExternalCorporationRepository.class);
    fileResourceMetadataService = mock(FileResourceMetadataService.class);
    when(fileResourceMetadataService.save(eq(null))).thenThrow(new NullPointerException());

    corporationService =
        new CorporationServiceImpl(
            corporationRepository, externalCorporationRepository, fileResourceMetadataService);
  }

  @Test
  @DisplayName("persists preview image for saved and retrieved corporation")
  void savePreviewImage() throws MalformedURLException, IdentifiableServiceException {
    Corporation corporation = mock(Corporation.class);
    ImageFileResource previewImageFileResource = mock(ImageFileResource.class);
    when(previewImageFileResource.getIiifBaseUrl()).thenReturn(new URL("file:///tmp/foo"));
    when(corporation.getPreviewImage()).thenReturn(previewImageFileResource);
    when(externalCorporationRepository.getByGndId(any(String.class))).thenReturn(corporation);

    assertThat(corporationService.fetchAndSaveByGndId("12345")).isNotNull();
    verify(fileResourceMetadataService, times(1)).save(any(FileResource.class));
  }

  @Test
  @DisplayName("can retrieve and save corporations without preview image")
  void saveWithoutPreviewImage() {
    Corporation corporationWithoutPreviewImage = new CorporationImpl();
    when(externalCorporationRepository.getByGndId(any(String.class)))
        .thenReturn(corporationWithoutPreviewImage);
    assertThat(corporationService.fetchAndSaveByGndId("12345")).isNotNull();
  }

  @Test
  @DisplayName("returns null when no corporation was found")
  void returnsNullForNullCorporation() {
    when(externalCorporationRepository.getByGndId(any(String.class))).thenReturn(null);
    assertThat(corporationService.fetchAndSaveByGndId("12345")).isNull();
  }
}
