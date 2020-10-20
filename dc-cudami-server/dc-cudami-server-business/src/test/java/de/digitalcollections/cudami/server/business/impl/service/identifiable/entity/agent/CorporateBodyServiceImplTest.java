package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.ExternalCorporateBodyRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.IdentifiableServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.FileResourceMetadataService;
import de.digitalcollections.model.api.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.api.identifiable.resource.FileResource;
import de.digitalcollections.model.api.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.impl.identifiable.entity.agent.CorporateBodyImpl;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The corporate body service")
class CorporateBodyServiceImplTest {

  private CorporateBodyServiceImpl corporateBodyService;
  private CorporateBodyRepository corporateBodyRepository;
  private ExternalCorporateBodyRepository externalCorporateBodyRepository;
  private FileResourceMetadataService fileResourceMetadataService;

  @BeforeEach
  void setUp() throws IdentifiableServiceException {
    corporateBodyRepository = mock(CorporateBodyRepository.class);
    when(corporateBodyRepository.save(any(CorporateBody.class)))
        .thenReturn(new CorporateBodyImpl());
    when(corporateBodyRepository.save(eq(null))).thenThrow(new NullPointerException());

    externalCorporateBodyRepository = mock(ExternalCorporateBodyRepository.class);
    fileResourceMetadataService = mock(FileResourceMetadataService.class);
    when(fileResourceMetadataService.save(eq(null))).thenThrow(new NullPointerException());

    corporateBodyService =
        new CorporateBodyServiceImpl(
            corporateBodyRepository, externalCorporateBodyRepository, fileResourceMetadataService);
  }

  @Test
  @DisplayName("persists preview image for saved and retrieved corporate body")
  void savePreviewImage() throws MalformedURLException, IdentifiableServiceException {
    CorporateBody corporateBody = mock(CorporateBody.class);
    ImageFileResource previewImageFileResource = mock(ImageFileResource.class);
    when(previewImageFileResource.getHttpBaseUrl()).thenReturn(new URL("file:///tmp/foo"));
    when(corporateBody.getPreviewImage()).thenReturn(previewImageFileResource);
    when(externalCorporateBodyRepository.getByGndId(any(String.class))).thenReturn(corporateBody);

    assertThat(corporateBodyService.fetchAndSaveByGndId("12345")).isNotNull();
    verify(fileResourceMetadataService, times(1)).save(any(FileResource.class));
  }

  @Test
  @DisplayName("can retrieve and save corporate bodies without preview image")
  void saveWithoutPreviewImage() {
    CorporateBody corporateBodyWithoutPreviewImage = new CorporateBodyImpl();
    when(externalCorporateBodyRepository.getByGndId(any(String.class)))
        .thenReturn(corporateBodyWithoutPreviewImage);
    assertThat(corporateBodyService.fetchAndSaveByGndId("12345")).isNotNull();
  }

  @Test
  @DisplayName("returns null when no corporate body was found")
  void returnsNullForNullCorporateBody() {
    when(externalCorporateBodyRepository.getByGndId(any(String.class))).thenReturn(null);
    assertThat(corporateBodyService.fetchAndSaveByGndId("12345")).isNull();
  }
}
