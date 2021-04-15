package de.digitalcollections.cudami.server.controller.v3.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.PageResponseBuilder;
import de.digitalcollections.cudami.server.model.WebpageBuilder;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V3WebpageController.class)
@DisplayName("The V3WebpageController")
class V3WebpageControllerTest extends BaseControllerTest {

  @MockBean private LocaleService localeService;
  @MockBean private WebpageService webpageService;

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
    Webpage expected = WebpageBuilder.createPrefilledWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);
    testJson(path);
  }

  @DisplayName("returns a webpage in explicit (accept header) v3 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?pLocale=de_DE"
      })
  public void returnWebpageV3JsonAcceptHeader(String path) throws Exception {
    Webpage expected = WebpageBuilder.createPrefilledWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);
    testJson(path);
  }

  @DisplayName("returns a webpage in default v3 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?pLocale=de_DE"
      })
  public void returnWebpageV3JsonDefault(String path) throws Exception {
    Webpage expected = WebpageBuilder.createPrefilledWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);
    testJson(path);
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
    testNotFound(path);
  }

  @DisplayName("returns active webpage")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?active=true",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?active=true&pLocale=de_DE"
      })
  public void returnActive(String path) throws Exception {
    Webpage expected = WebpageBuilder.createPrefilledWebpage(path);
    when(webpageService.getActive(any(UUID.class))).thenReturn(expected);
    when(webpageService.getActive(any(UUID.class), any(Locale.class))).thenReturn(expected);
    testJson(path);
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
    Webpage expected = WebpageBuilder.createPrefilledWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);
    testXml(path);
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
    Webpage expected = WebpageBuilder.createPrefilledWebpage(path);
    when(webpageService.get(any(UUID.class))).thenReturn(expected);
    when(webpageService.get(any(UUID.class), any(Locale.class))).thenReturn(expected);
    testXml(path);
  }

  @DisplayName("returns the children of a webpage")
  @ParameterizedTest
  @ValueSource(strings = {"/v3/webpages/157f5428-5a5a-4d47-971e-f092f1836246/children"})
  public void returnChildrenOfAWebpage(String path) throws Exception {
    PageResponse<Webpage> expected =
        (PageResponse)
            new PageResponseBuilder<>()
                .forRequestPage(0)
                .forPageSize(25)
                .withContent(
                    List.of(
                        new WebpageBuilder()
                            .createdAt("2020-07-07T17:09:33.375772")
                            .withLabel(Locale.GERMAN, "Kontakt")
                            .withLabel(Locale.ENGLISH, "Contact")
                            .lastModifiedAt("2021-03-23T11:22:05.314403")
                            .withUuid("5f92d901-8171-49da-9b6c-7201f545e944")
                            .withPublicationStartAt("2020-07-07")
                            .notShownInNavigation()
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2019-09-09T15:02:35.186941")
                            .withLabel(Locale.GERMAN, "Impressum")
                            .withLabel(Locale.ENGLISH, "Imprint")
                            .lastModifiedAt("2021-03-22T09:13:58.513396")
                            .withUuid("7d2244c7-9e8a-40ed-9806-5618b6e64a87")
                            .withPublicationStartAt("2020-07-07")
                            .shownInNavigation()
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2019-09-09T15:03:02.077221")
                            .withLabel(Locale.GERMAN, "Datenschutzerklärung")
                            .withLabel(Locale.ENGLISH, "Privacy Policy")
                            .lastModifiedAt("2021-03-22T09:16:23.327765")
                            .withUuid("452ae4e9-b10f-4824-9b75-29f32ac89c34")
                            .withPublicationStartAt("2020-07-07")
                            .shownInNavigation()
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2020-03-12T12:38:45.902257")
                            .withLabel(Locale.GERMAN, "Barrierefreiheit")
                            .withLabel(Locale.ENGLISH, "Accessibility")
                            .lastModifiedAt("2021-03-22T09:17:40.237713")
                            .withUuid("fc8a8363-4091-412f-8cc6-5e2386deea94")
                            .withPublicationStartAt("2020-07-07")
                            .shownInNavigation()
                            .build()))
                .build();

    when(webpageService.getChildren(any(UUID.class), any(PageRequest.class))).thenReturn(expected);
    testJson(path);
  }

  @DisplayName("returns a website")
  @ParameterizedTest
  @ValueSource(strings = {"/v3/webpages/6d52141c-5c5d-48b4-aee8-7df5404d245e/website"})
  @Disabled("TODO")
  public void returnWebsite(String path) throws Exception {}
}
