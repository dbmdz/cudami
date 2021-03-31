package de.digitalcollections.cudami.server.controller.v3.identifiable.web;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

@WebMvcTest(V3WebpageController.class)
@DisplayName("The V3WebpageController")
class V3WebpageControllerTest extends BaseWebpageControllerTest {

  @DisplayName("returns a webpage in explicit (url) v3 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.json?pLocale=de_DE"
      })
  public void returnWebpageV3JsonUrl(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  @DisplayName("returns a webpage in explicit (accept header) v3 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?pLocale=de_DE"
      })
  public void returnWebpageV3JsonAcceptHeader(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  @DisplayName("returns a webpage in default v3 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?pLocale=de_DE"
      })
  public void returnWebpageV3JsonDefault(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  @DisplayName("does not return a non active webpage")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?active=true",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?active=true&pLocale=de_DE"
      })
  public void returnNoNonActive(String path) throws Exception {
    when(webpageService.getActive(any(UUID.class))).thenReturn(null);
    when(webpageService.getActive(any(UUID.class), any(Locale.class))).thenReturn(null);

    mockMvc.perform(get(path)).andExpect(status().isNotFound());
  }

  @DisplayName("returns active webpage")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?active=true",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?active=true&pLocale=de_DE"
      })
  public void returnActive(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.getActive(any(UUID.class))).thenReturn(expected);
    when(webpageService.getActive(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  @DisplayName("returns a webpage in explicit (url) v3 xml format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.xml",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa.xml?pLocale=de_DE"
      })
  @Disabled("does not support XML results any more since they were never used")
  public void returnWebpageV3XmlUrl(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(
            content().contentType(ContentType.APPLICATION_XML.getMimeType() + ";charset=UTF-8"))
        .andExpect(content().xml(getXmlFromFileResource(path)));
  }

  @DisplayName("returns a webpage in explicit (accept header) v3 xml format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?pLocale=de_DE"
      })
  @Disabled("does not support XML results any more since they were never used")
  public void returnWebpageV3XmlAcceptHeader(String path) throws Exception {
    Webpage expected = createWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);

    mockMvc
        .perform(get(path).accept(MediaType.APPLICATION_XML))
        .andExpect(status().isOk())
        .andExpect(
            content().contentType(ContentType.APPLICATION_XML.getMimeType() + ";charset=UTF-8"))
        .andExpect(content().xml(getXmlFromFileResource(path)));
  }
}
