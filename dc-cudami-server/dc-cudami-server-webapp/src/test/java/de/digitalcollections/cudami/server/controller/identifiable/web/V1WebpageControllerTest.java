package de.digitalcollections.cudami.server.controller.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V1WebpageController.class)
class V1WebpageControllerTest extends BaseWebpageControllerTest {

  @MockBean private WebpageService webpageService;

  @DisplayName("returns a localized webpage in v1 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json?pLocale=de_DE",
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?pLocale=de_DE"
      })
  public void returnLocalizedWebpageV1Json(String path) throws Exception {
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getByExampleAndLocale(any(Webpage.class), any(Locale.class)))
        .thenReturn(expected);
    testJson(path);
  }

  @DisplayName("returns a webpage in v1 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json",
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa"
      })
  public void returnWebpageV1Json(String path) throws Exception {
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getByExample(any(Webpage.class))).thenReturn(expected);
    testJson(path);
  }

  @DisplayName(
      "returns a localized webpage in v1 json format for UUID and a wrong locale parameter")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json?locale=de_DE",
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?locale=de_DE"
      })
  public void returnWrongLocalizedWebpageV1Json(String path) throws Exception {
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getByExample(any(Webpage.class))).thenReturn(expected);
    testJson(path);
  }
}
