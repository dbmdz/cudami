package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.CorporateBodyRepository;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.entity.agent.ExternalCorporateBodyRepository;
import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.alias.UrlAliasService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.resource.ImageFileResourceService;
import de.digitalcollections.cudami.server.business.impl.service.AbstractServiceImplTest;
import de.digitalcollections.cudami.server.config.HookProperties;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.resource.ImageFileResource;
import de.digitalcollections.model.validation.ValidationException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The corporate body service")
class CorporateBodyServiceImplTest extends AbstractServiceImplTest {

  private CorporateBodyServiceImpl corporateBodyService;
  private CorporateBodyRepository corporateBodyRepository;
  private ExternalCorporateBodyRepository externalCorporateBodyRepository;
  private ImageFileResourceService imageFileResourceService;
  private IdentifierService identifierService;
  private UrlAliasService urlAliasService;

  @Override
  @BeforeEach
  public void beforeEach() throws Exception {
    super.beforeEach();
    corporateBodyRepository = mock(CorporateBodyRepository.class);
    doThrow(NullPointerException.class).when(corporateBodyRepository).save(eq(null));

    externalCorporateBodyRepository = mock(ExternalCorporateBodyRepository.class);
    imageFileResourceService = mock(ImageFileResourceService.class);
    doThrow(NullPointerException.class).when(imageFileResourceService).save(eq(null));

    identifierService = mock(IdentifierService.class);
    urlAliasService = mock(UrlAliasService.class);

    HookProperties hookProperties = mock(HookProperties.class);

    LocaleService localeService = mock(LocaleService.class);

    corporateBodyService =
        new CorporateBodyServiceImpl(
            corporateBodyRepository,
            externalCorporateBodyRepository,
            imageFileResourceService,
            identifierService,
            urlAliasService,
            hookProperties,
            localeService,
            cudamiConfig);
  }

  @Test
  @DisplayName("persists preview image for saved and retrieved corporate body")
  void savePreviewImage() throws MalformedURLException, ServiceException, ValidationException {
    CorporateBody corporateBody = mock(CorporateBody.class);
    ImageFileResource previewImageFileResource = mock(ImageFileResource.class);
    when(previewImageFileResource.getHttpBaseUrl()).thenReturn(new URL("file:///tmp/foo"));
    when(corporateBody.getPreviewImage()).thenReturn(previewImageFileResource);
    when(externalCorporateBodyRepository.getByGndId(any(String.class))).thenReturn(corporateBody);

    assertThat(corporateBodyService.fetchAndSaveByGndId("12345")).isNotNull();
    verify(imageFileResourceService, times(1)).save(any(ImageFileResource.class));
  }

  @Test
  @DisplayName("can retrieve and save corporate bodies without preview image")
  void saveWithoutPreviewImage() throws ServiceException, ValidationException {
    CorporateBody corporateBodyWithoutPreviewImage = new CorporateBody();
    when(externalCorporateBodyRepository.getByGndId(any(String.class)))
        .thenReturn(corporateBodyWithoutPreviewImage);
    assertThat(corporateBodyService.fetchAndSaveByGndId("12345")).isNotNull();
  }

  @Test
  @DisplayName("returns null when no corporate body was found")
  void returnsNullForNullCorporateBody() throws ServiceException, ValidationException {
    when(externalCorporateBodyRepository.getByGndId(any(String.class))).thenReturn(null);
    assertThat(corporateBodyService.fetchAndSaveByGndId("12345")).isNull();
  }
}
