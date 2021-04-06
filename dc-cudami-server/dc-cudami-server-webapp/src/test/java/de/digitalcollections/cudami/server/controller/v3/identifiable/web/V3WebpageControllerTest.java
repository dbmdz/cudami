package de.digitalcollections.cudami.server.controller.v3.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.controller.BaseWebpageControllerTest;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

  // TODO: test /latest/webpages/<uuid>/children
  // TODO: test webpages and webpages/childen with active flag
  // TODO: test latest/webpages/<uuid>/childrentree with and withput active flag

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

  @DisplayName("returns the children of a webpage")
  @ParameterizedTest
  @ValueSource(strings = {"/v3/webpages/157f5428-5a5a-4d47-971e-f092f1836246/children"})
  public void returnChildrenOfAWebpage(String path) throws Exception {
    List<Webpage> webpages =
        List.of(
            createMetaWebpage(
                "2020-07-07T17:09:33.375772",
                Map.of(Locale.GERMAN, "Kontakt", Locale.ENGLISH, "Contact"),
                "2021-03-23T11:22:05.314403",
                "5f92d901-8171-49da-9b6c-7201f545e944",
                "2020-07-07",
                false),
            createMetaWebpage(
                "2019-09-09T15:02:35.186941",
                Map.of(Locale.GERMAN, "Impressum", Locale.ENGLISH, "Imprint"),
                "2021-03-22T09:13:58.513396",
                "7d2244c7-9e8a-40ed-9806-5618b6e64a87",
                "2020-07-07",
                true),
            createMetaWebpage(
                "2019-09-09T15:03:02.077221",
                Map.of(Locale.GERMAN, "Datenschutzerklärung", Locale.ENGLISH, "Privacy Policy"),
                "2021-03-22T09:16:23.327765",
                "452ae4e9-b10f-4824-9b75-29f32ac89c34",
                "2020-07-07",
                true),
            createMetaWebpage(
                "2020-03-12T12:38:45.902257",
                Map.of(Locale.GERMAN, "Barrierefreiheit", Locale.ENGLISH, "Accessibility"),
                "2021-03-22T09:17:40.237713",
                "fc8a8363-4091-412f-8cc6-5e2386deea94",
                "2020-07-07",
                true));

    PageResponse<Webpage> children = buildStandardPageResponse(Webpage.class, webpages);
    when(webpageService.getChildren(any(UUID.class), any(PageRequest.class))).thenReturn(children);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }

  @DisplayName("returns a website")
  @ParameterizedTest
  @ValueSource(strings = {"/v3/webpages/6d52141c-5c5d-48b4-aee8-7df5404d245e/website"})
  public void returnWebsite(String path) throws Exception {}
}
