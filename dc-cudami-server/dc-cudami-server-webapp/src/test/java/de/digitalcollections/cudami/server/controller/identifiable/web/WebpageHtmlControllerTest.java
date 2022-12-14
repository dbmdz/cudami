package de.digitalcollections.cudami.server.controller.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(WebpageHtmlController.class)
public class WebpageHtmlControllerTest extends BaseWebpageControllerTest {

  private static final boolean IGNORE_WHITESPACES = true;

  @MockBean protected WebpageService webpageService;

  @DisplayName("returns a webpage in v1 HTML format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.html",
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.html?pLocale=de_DE"
      })
  public void returnWebpageV1Html(String path) throws Exception {
    Webpage expected = createPrefilledWebpage(path);

    when(webpageService.getByUuid(any(UUID.class))).thenReturn(expected);
    when(webpageService.getByUuidAndLocale(any(UUID.class), any(Locale.class)))
        .thenReturn(expected);

    testHtml(path);
  }

  @DisplayName("returns a webpage in v3 html format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.html",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.html?pLocale=de_DE"
      })
  public void returnWebpageV3Html(String path) throws Exception {
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getByUuid(any(UUID.class))).thenReturn(expected);
    when(webpageService.getByUuidAndLocale(any(UUID.class), any(Locale.class)))
        .thenReturn(expected);

    testHtml(path);
  }
}
