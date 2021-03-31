package de.digitalcollections.cudami.server.controller.v1.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.controller.BaseWebpageControllerTest;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.Locale;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(V1WebpageController.class)
class V1WebpageControllerTest extends BaseWebpageControllerTest {

  @DisplayName("returns a webpage in v1 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json",
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa"
      })
  public void returnWebpageV1Json(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  @DisplayName("returns a localized webpage in v1 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json?pLocale=de_DE",
        "/v1/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?pLocale=de_DE"
      })
  public void returnLocalizedWebpageV1Json(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
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
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }
}
