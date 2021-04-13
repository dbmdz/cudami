package de.digitalcollections.cudami.server.controller;

import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import java.util.Arrays;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(LocaleController.class)
@DisplayName("The LocaleController")
class LocaleControllerTest extends BaseControllerTest {

  @MockBean private LocaleService localeService;

  @DisplayName("returns the default language")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/languages/default"})
  public void defaultLanguage(String path) throws Exception {
    when(localeService.getDefaultLanguage()).thenReturn("de");

    testJson(path);
  }

  @DisplayName("returns all languages")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/languages"})
  public void allLanguages(String path) throws Exception {
    when(localeService.getSupportedLanguages()).thenReturn(Arrays.asList(Locale.getISOLanguages()));

    testJson(path, "/v2/languages/languages.json");
  }

  @DisplayName("returns the default locale")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/locales/default"})
  public void defaultLocale(String path) throws Exception {
    when(localeService.getDefaultLocale()).thenReturn(Locale.GERMANY);

    testJson(path);
  }

  @DisplayName("returns all locales")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/locales"})
  public void allLocales(String path) throws Exception {
    when(localeService.getSupportedLocales())
        .thenReturn(Arrays.asList(Locale.getAvailableLocales()));

    testJson(path, "/v2/locales/locales.json");
  }
}
