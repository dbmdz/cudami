package io.github.dbmdz.cudami.controller.identifiable.semantic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.client.CudamiClient;
import de.digitalcollections.cudami.client.identifiable.entity.semantic.CudamiSubjectsClient;
import de.digitalcollections.model.exception.ResourceNotFoundException;
import de.digitalcollections.model.exception.TechnicalException;
import de.digitalcollections.model.identifiable.semantic.Subject;
import io.github.dbmdz.cudami.business.i18n.LanguageService;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

@DisplayName("The SubjectsController")
class SubjectsControllerTest {

  private LanguageService languageService;
  private SubjectsController subjectsController;
  private CudamiSubjectsClient subjectsClient;

  @BeforeEach
  public void beforeEach() {
    languageService = mock(LanguageService.class);
    CudamiClient cudamiClient = mock(CudamiClient.class);
    subjectsClient = mock(CudamiSubjectsClient.class);
    when(cudamiClient.forSubjects()).thenReturn(subjectsClient);
    subjectsController = new SubjectsController(cudamiClient, languageService, null, null);
  }

  @DisplayName("lets then view use first data language, if no target data language is set")
  @Test
  public void viewUsesFirstDataLanguage() throws TechnicalException, ResourceNotFoundException {
    Subject subject =
        Subject.builder()
            .label(Locale.forLanguageTag("de-Latn"), "deutsch")
            .label(Locale.forLanguageTag("la-Latn"), "Latinum")
            .build();
    when(subjectsClient.getByUuid(any(UUID.class))).thenReturn(subject);
    when(languageService.getDefaultLanguage()).thenReturn(Locale.ENGLISH);
    when(languageService.sortLanguages(any(), any()))
        .thenReturn(List.of(Locale.forLanguageTag("de-Latn"), Locale.forLanguageTag("la-Latn")));

    Model model = new ConcurrentModel();
    subjectsController.view(UUID.randomUUID(), null, model);

    assertThat((String) model.getAttribute("dataLanguage")).isEqualTo("de-Latn");
  }

  @DisplayName("lets then view use the target data language, when the target data language is set")
  @Test
  public void viewUsesTargetDataLanguage() throws TechnicalException, ResourceNotFoundException {
    Subject subject =
        Subject.builder()
            .label(Locale.forLanguageTag("de-Latn"), "deutsch")
            .label(Locale.forLanguageTag("la-Latn"), "Latinum")
            .build();
    when(subjectsClient.getByUuid(any(UUID.class))).thenReturn(subject);
    when(languageService.getDefaultLanguage()).thenReturn(Locale.ENGLISH);
    when(languageService.sortLanguages(any(), any()))
        .thenReturn(List.of(Locale.forLanguageTag("de-Latn"), Locale.forLanguageTag("la-Latn")));

    Model model = new ConcurrentModel();
    subjectsController.view(UUID.randomUUID(), "la-Latn", model);

    assertThat((String) model.getAttribute("dataLanguage")).isEqualTo("la-Latn");
  }

  @DisplayName("throws a ResourceNotFoundException, when no resource could be found")
  @Test
  public void resourceNotFoundException() throws TechnicalException, ResourceNotFoundException {
    when(subjectsClient.getByUuid(any(UUID.class))).thenReturn(null);
    assertThatExceptionOfType(ResourceNotFoundException.class)
        .isThrownBy(() -> subjectsController.view(UUID.randomUUID(), null, new ConcurrentModel()));
  }
}
