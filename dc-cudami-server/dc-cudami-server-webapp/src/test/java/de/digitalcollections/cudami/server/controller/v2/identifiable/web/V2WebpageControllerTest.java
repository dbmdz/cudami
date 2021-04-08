package de.digitalcollections.cudami.server.controller.v2.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

@WebMvcTest(V2WebpageController.class)
public class V2WebpageControllerTest extends BaseWebpageControllerTest {

  @MockBean protected WebpageService webpageService;

  @DisplayName(
      "returns a webpage in v2 json format for UUID, with or without json suffix in the url")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v2/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json",
        "/v2/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa"
      })
  public void returnWebpageV2Json(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  @DisplayName(
      "returns a localized webpage in v2 json format for UUID, with or without json suffix in the url")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v2/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json?pLocale=de",
        "/v2/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?pLocale=de"
      })
  public void returnLocalizedWebpageV2Json(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }
}
