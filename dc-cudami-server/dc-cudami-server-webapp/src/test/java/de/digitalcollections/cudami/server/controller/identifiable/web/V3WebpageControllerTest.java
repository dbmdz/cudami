package de.digitalcollections.cudami.server.controller.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.model.file.MimeType;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.identifiable.web.WebpageBuilder;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.PageResponseBuilder;
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
class V3WebpageControllerTest extends BaseWebpageControllerTest {

  @MockBean private LocaleService localeService;
  @MockBean private WebpageService webpageService;

  @DisplayName("returns active webpage")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?active=true",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?active=true&pLocale=de_DE"
      })
  public void returnActive(String path) throws Exception {
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getActive(any(UUID.class))).thenReturn(expected);
    when(webpageService.getActive(any(UUID.class), any(Locale.class))).thenReturn(expected);
    testJson(path);
  }

  @DisplayName("returns the (active) children of a webpage")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/latest/webpages/157f5428-5a5a-4d47-971e-f092f1836246/children",
        "/v3/webpages/157f5428-5a5a-4d47-971e-f092f1836246/children",
        "/v3/webpages/157f5428-5a5a-4d47-971e-f092f1836246/children?active=true"
      })
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
    when(webpageService.getActiveChildren(any(UUID.class), any(PageRequest.class)))
        .thenReturn(expected);
    testJson(path.replaceAll("latest", "v3")); // v3 equals latest
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

  @DisplayName("keeps the sorting order of the children of a webpage")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/latest/webpages/5765b8bf-698c-4678-9ffa-bda77c5f44b5/children?sortBy=publicationStart.desc&pageSize=5",
        "/v3/webpages/5765b8bf-698c-4678-9ffa-bda77c5f44b5/children?sortBy=publicationStart.desc&pageSize=5"
      })
  public void returnSortOrderOfChildrenOfAWebpage(String path) throws Exception {
    PageResponse<Webpage> expected =
        (PageResponse)
            new PageResponseBuilder<>()
                .forRequestPage(0)
                .forPageSize(5)
                .forDescendingOrderedField("publicationStart")
                .withTotalElements(81)
                .withContent(
                    List.of(
                        new WebpageBuilder()
                            .createdAt("2021-04-19T09:36:44.248372")
                            .withLabel(Locale.GERMAN, "1")
                            .lastModifiedAt("2021-04-19T09:37:21.839009")
                            .withUuid("ed86dcc0-a1aa-4e82-872a-8d82d2536846")
                            .withPublicationStartAt("2021-04-19")
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2020-05-15T11:14:44.52781")
                            .withLabel(Locale.GERMAN, "2")
                            .lastModifiedAt("2020-07-01T17:31:36.627785")
                            .withUuid("97113300-3b04-4b6f-9082-294d10fd778b")
                            .withPublicationStartAt("2020-06-30")
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2020-05-15T11:14:42.588253")
                            .withLabel(Locale.GERMAN, "3")
                            .lastModifiedAt("2021-02-04T08:29:18.210378")
                            .withUuid("5c2cd93c-73f5-4abd-b200-9141c5473df1")
                            .withPublicationStartAt("2020-05-29")
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2020-05-15T11:14:43.640695")
                            .withLabel(Locale.GERMAN, "4")
                            .lastModifiedAt("2020-11-05T16:52:42.353087")
                            .withUuid("38e4b7b0-b75a-4ded-a109-4213352130fa")
                            .withPublicationStartAt("2020-05-29")
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2020-05-15T11:14:45.997827")
                            .withLabel(Locale.GERMAN, "5")
                            .lastModifiedAt("2020-05-29T16:31:23.556613")
                            .withUuid("da9cbead-d855-434c-9c78-69a5d077f9a2")
                            .withPublicationStartAt("2020-05-29")
                            .build()))
                .build();

    when(webpageService.getChildren(any(UUID.class), any(PageRequest.class))).thenReturn(expected);
    when(webpageService.getActiveChildren(any(UUID.class), any(PageRequest.class)))
        .thenReturn(expected);
    testJson(path, "/v3/webpages/news.json");
  }

  @DisplayName("returns a webpage in explicit (accept header) v3 json format for UUID")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa",
        "/v3/webpages/8f95bd0a-7095-44e7-9ab3-061f288741aa?pLocale=de_DE"
      })
  public void returnWebpageV3JsonAcceptHeader(String path) throws Exception {
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getByUuid(any(UUID.class))).thenReturn(expected);
    when(webpageService.getByUuidAndLocale(any(UUID.class), any(Locale.class)))
        .thenReturn(expected);
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
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getByUuid(any(UUID.class))).thenReturn(expected);
    when(webpageService.getByUuidAndLocale(any(UUID.class), any(Locale.class)))
        .thenReturn(expected);
    testJson(path);
  }
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
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getByUuid(any(UUID.class))).thenReturn(expected);
    when(webpageService.getByUuidAndLocale(any(UUID.class), any(Locale.class)))
        .thenReturn(expected);
    testJson(path);
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
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getByUuid(any(UUID.class))).thenReturn(expected);
    when(webpageService.getByUuidAndLocale(any(UUID.class), any(Locale.class)))
        .thenReturn(expected);
    testXml(path);
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
    Webpage expected = createPrefilledWebpage(path);
    when(webpageService.getByUuid(any(UUID.class))).thenReturn(expected);
    when(webpageService.getByUuidAndLocale(any(UUID.class), any(Locale.class)))
        .thenReturn(expected);
    testXml(path);
  }

  @DisplayName("returns a webpage with a tree of its children")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/webpages/ead664b6-5fcc-414e-b3bb-133f0af1acb8/childrentree",
        "/v3/webpages/ead664b6-5fcc-414e-b3bb-133f0af1acb8/childrentree?active=true"
      })
  public void returnWebpageWithChildrentree(String path) throws Exception {
    List<Webpage> expected =
        List.of(
            new WebpageBuilder()
                .createdAt("2019-10-31T11:25:10.696785")
                .withDescription("de", "Test-Text")
                .withLabel(Locale.GERMAN, "Test")
                .lastModifiedAt("2020-10-20T14:36:29.017223")
                .withPreviewImage(
                    "test.jpg",
                    "b2fdfbc2-833c-4e11-a80c-a3331d09e10b",
                    "file:///cudami/image/jpg/b2fd/fbc2/833c/4e11/a80c/a333/1d09/e10b/resource.jpg",
                    MimeType.MIME_IMAGE_JPEG,
                    "https://api.digitale-sammlungen.de/iiif/image/v2/b2fdfbc2-833c-4e11-a80c-a3331d09e10b")
                .withAltText(Locale.GERMAN, "test alt")
                .withOpenLinkInNewWindow()
                .withUuid("4c202033-60e2-4a21-9fd7-660cadae95d5")
                .withPublicationStartAt("2020-10-01")
                .withChildren(
                    List.of(
                        new WebpageBuilder()
                            .createdAt("2019-10-31T11:26:26.357575")
                            .withLabel(Locale.GERMAN, "Technologien und Softwareentwicklung")
                            .lastModifiedAt("2021-01-11T14:20:33.920896")
                            .withUuid("d329196f-1a5e-4e45-aa7a-857417f2fb5b")
                            .withPublicationStartAt("2019-12-09")
                            .notShownInNavigation()
                            .withChildren(
                                List.of(
                                    new WebpageBuilder()
                                        .createdAt("2019-12-09T13:40:54.644153")
                                        .withLabel(Locale.GERMAN, "IIIF und Mirador")
                                        .lastModifiedAt("2019-12-09T13:41:28.196622")
                                        .withUuid("24500113-d0ce-4411-ad2d-db92c7c6939f")
                                        .withPublicationStartAt("2019-12-09")
                                        .build()))
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2019-10-31T11:26:41.189086")
                            .withLabel(Locale.GERMAN, "Test1")
                            .lastModifiedAt("2019-12-09T14:02:08.693881")
                            .withUuid("b383e248-82d5-405b-b33b-9d879913044a")
                            .withPublicationStartAt("2019-12-09")
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2019-10-31T11:26:52.877568")
                            .withLabel(Locale.GERMAN, "Test2")
                            .withLabel(Locale.ENGLISH, "test2")
                            .lastModifiedAt("2021-01-11T15:27:36.011534")
                            .withUuid("f1ef5feb-3d19-4888-b67e-2efae4bda2ab")
                            .withPublicationStartAt("2020-11-02")
                            .shownInNavigation()
                            .withTemplateName("timeline")
                            .build()))
                .build(),
            new WebpageBuilder()
                .createdAt("2019-10-31T11:27:58.409698")
                .withLabel(Locale.GERMAN, "Hilfe")
                .lastModifiedAt("2020-10-19T16:08:52.672063")
                .withUuid("8f0b1760-a03e-47b0-862a-0f234e5a79d4")
                .withPublicationStartAt("2020-10-19")
                .withChildren(
                    List.of(
                        new WebpageBuilder()
                            .createdAt("2019-10-31T11:28:14.191666")
                            .withLabel(Locale.GERMAN, "Hilfe zur Suche")
                            .lastModifiedAt("2021-02-11T10:52:01.433945")
                            .withUuid("5c26f7ca-5a36-47ee-9bed-4cb816c49e79")
                            .withPublicationStartAt("2019-11-05")
                            .shownInNavigation()
                            .build()))
                .build());

    when(webpageService.getChildrenTree(
            eq(UUID.fromString("ead664b6-5fcc-414e-b3bb-133f0af1acb8"))))
        .thenReturn(expected);
    when(webpageService.getActiveChildrenTree(
            eq(UUID.fromString("ead664b6-5fcc-414e-b3bb-133f0af1acb8"))))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("returns a website")
  @ParameterizedTest
  @ValueSource(strings = {"/v3/webpages/6d52141c-5c5d-48b4-aee8-7df5404d245e/website"})
  @Disabled("TODO")
  public void returnWebsite(String path) throws Exception {}
}
