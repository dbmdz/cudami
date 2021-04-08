package de.digitalcollections.cudami.server.controller.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.controller.BaseWebpageControllerTest;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.Locale;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(WebpageHtmlController.class)
public class WebpageHtmlControllerTest extends BaseWebpageControllerTest {

  @MockBean protected WebpageService webpageService;

  @DisplayName("returns a webpage in v1 HTML format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.html",
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.html?pLocale=de_DE"
      })
  public void returnWebpageV1Html(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.TEXT_HTML.getMimeType() + ";charset=UTF-8"))
        .andExpect(content().string(getHtmlFromFileResource(path)));
  }

  @DisplayName("returns a webpage in v3 html format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.html",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.html?pLocale=de_DE"
      })
  public void returnWebpageV3Html(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.TEXT_HTML.getMimeType() + ";charset=UTF-8"))
        .andExpect(content().string(getHtmlFromFileResource(path)));
  }
}
