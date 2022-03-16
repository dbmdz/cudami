package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.entity.WebsiteBuilder;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.identifiable.web.WebpageBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.SearchPageResponseBuilder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V3WebsiteController.class)
@DisplayName("The V3WebsiteController")
public class V3WebsiteControllerTest extends BaseControllerTest {

  @MockBean private WebsiteService websiteService;

  @DisplayName("returns the paged rootpages")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/websites/7a2f1935-c5b8-40fb-8622-c675de0a6242/rootpages?pageNumber=0&pageSize=20"
      })
  public void pagedRootpages(String path) throws Exception {
    SearchPageResponse<Webpage> expected =
        (SearchPageResponse<Webpage>)
            new SearchPageResponseBuilder()
                .forPageSize(25)
                .forRequestPage(0)
                .withTotalElements(4)
                .withContent(
                    List.of(
                        new WebpageBuilder()
                            .createdAt("2019-08-19T15:04:29.397957")
                            .withLabel(Locale.GERMAN, "Startseite")
                            .withLabel(Locale.ENGLISH, "MDZ Homepage")
                            .lastModifiedAt("2021-03-31T11:36:03.58095")
                            .withUuid("76c5d90f-1a72-47ed-819e-f9e86328a304")
                            .withPublicationStartAt("2020-10-01")
                            .shownInNavigation()
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2019-09-09T15:05:17.356311")
                            .withLabel(Locale.GERMAN, "News")
                            .lastModifiedAt("2019-09-09T15:05:17.356343")
                            .withUuid("692b3792-f5d9-4e6d-9bb6-7fff6345467a")
                            .withPublicationStartAt("2019-09-09")
                            .notShownInNavigation()
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2020-06-30T11:27:57.050954")
                            .withDescription(
                                "de",
                                "technischer Knoten, der alle Webpages des Hauptmenüs enthält")
                            .withLabel(Locale.GERMAN, "Hauptmenü")
                            .lastModifiedAt("2020-06-30T11:27:57.050963")
                            .withUuid("6d52141c-5c5d-48b4-aee8-7df5404d245e")
                            .withPublicationStartAt("2020-06-30")
                            .build(),
                        new WebpageBuilder()
                            .createdAt("2020-07-07T17:04:52.129368")
                            .withLabel(Locale.GERMAN, "Footer")
                            .lastModifiedAt("2020-07-07T17:04:52.129378")
                            .withUuid("157f5428-5a5a-4d47-971e-f092f1836246")
                            .withPublicationStartAt("2020-07-07")
                            .notShownInNavigation()
                            .build()))
                .build();

    when(websiteService.findRootPages(any(UUID.class), any(SearchPageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("returns a website by its uuid")
  @ParameterizedTest
  @ValueSource(strings = {"/v3/websites/7a2f1935-c5b8-40fb-8622-c675de0a6242"})
  public void websiteByUuid(String path) throws Exception {
    Website expected =
        new WebsiteBuilder()
            .withUuid(extractFirstUuidFromPath(path))
            .createdAt("2019-08-12T16:28:52.171814")
            .withLabel(Locale.GERMAN, "MDZ Homepage")
            .lastModifiedAt("2019-12-02T12:46:46.6262")
            .withRefId(29)
            .withUrl("https://www.digitale-sammlungen.de/")
            .withRootPages(
                List.of(
                    new WebpageBuilder()
                        .createdAt("2020-07-07T17:04:52.129368")
                        .withLabel(Locale.GERMAN, "Footer")
                        .lastModifiedAt("2020-07-07T17:04:52.129378")
                        .withUuid("157f5428-5a5a-4d47-971e-f092f1836246")
                        .withPublicationStartAt("2020-07-07")
                        .build(),
                    new WebpageBuilder()
                        .createdAt("2019-09-09T15:05:17.356311")
                        .withLabel(Locale.GERMAN, "News")
                        .lastModifiedAt("2019-09-09T15:05:17.356343")
                        .withUuid("692b3792-f5d9-4e6d-9bb6-7fff6345467a")
                        .withPublicationStartAt("2019-09-09")
                        .notShownInNavigation()
                        .build(),
                    new WebpageBuilder()
                        .createdAt("2020-06-30T11:27:57.050954")
                        .withDescription(
                            "de", "technischer Knoten, der alle Webpages des Hauptmenüs enthält")
                        .withLabel(Locale.GERMAN, "Hauptmenü")
                        .lastModifiedAt("2020-06-30T11:27:57.050963")
                        .withUuid("6d52141c-5c5d-48b4-aee8-7df5404d245e")
                        .withPublicationStartAt("2020-06-30")
                        .build(),
                    new WebpageBuilder()
                        .createdAt("2019-08-19T15:04:29.397957")
                        .withLabel(Locale.GERMAN, "Startseite")
                        .withLabel(Locale.ENGLISH, "MDZ Homepage")
                        .lastModifiedAt("2021-03-31T11:36:03.58095")
                        .withUuid("76c5d90f-1a72-47ed-819e-f9e86328a304")
                        .withPublicationStartAt("2020-10-01")
                        .shownInNavigation()
                        .build()))
            .build();

    when(websiteService.getByUuid(any(UUID.class))).thenReturn(expected);

    testJson(path);
  }
}
