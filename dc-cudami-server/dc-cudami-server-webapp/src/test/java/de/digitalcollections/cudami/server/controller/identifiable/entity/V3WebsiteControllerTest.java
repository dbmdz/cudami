package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
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
            SearchPageResponse.builder()
                .forPageSize(25)
                .forRequestPage(0)
                .withTotalElements(4)
                .withContent(
                    List.of(
                        Webpage.builder()
                            .created("2019-08-19T15:04:29.397957")
                            .label(Locale.GERMAN, "Startseite")
                            .label(Locale.ENGLISH, "MDZ Homepage")
                            .lastModified("2021-03-31T11:36:03.58095")
                            .uuid("76c5d90f-1a72-47ed-819e-f9e86328a304")
                            .publicationStart("2020-10-01")
                            .shownInNavigation()
                            .build(),
                        Webpage.builder()
                            .created("2019-09-09T15:05:17.356311")
                            .label(Locale.GERMAN, "News")
                            .lastModified("2019-09-09T15:05:17.356343")
                            .uuid("692b3792-f5d9-4e6d-9bb6-7fff6345467a")
                            .publicationStart("2019-09-09")
                            .notShownInNavigation()
                            .build(),
                        Webpage.builder()
                            .created("2020-06-30T11:27:57.050954")
                            .description(
                                "de",
                                "technischer Knoten, der alle Webpages des Hauptmenüs enthält")
                            .label(Locale.GERMAN, "Hauptmenü")
                            .lastModified("2020-06-30T11:27:57.050963")
                            .uuid("6d52141c-5c5d-48b4-aee8-7df5404d245e")
                            .publicationStart("2020-06-30")
                            .build(),
                        Webpage.builder()
                            .created("2020-07-07T17:04:52.129368")
                            .label(Locale.GERMAN, "Footer")
                            .lastModified("2020-07-07T17:04:52.129378")
                            .uuid("157f5428-5a5a-4d47-971e-f092f1836246")
                            .publicationStart("2020-07-07")
                            .notShownInNavigation()
                            .build()))
                .build();

    when(websiteService.findRootWebpages(any(UUID.class), any(SearchPageRequest.class)))
        .thenReturn(expected);

    testJson(path);
  }

  @DisplayName("returns a website by its uuid")
  @ParameterizedTest
  @ValueSource(strings = {"/v3/websites/7a2f1935-c5b8-40fb-8622-c675de0a6242"})
  public void websiteByUuid(String path) throws Exception {
    Website expected =
        Website.builder()
            .uuid(extractFirstUuidFromPath(path))
            .created("2019-08-12T16:28:52.171814")
            .label(Locale.GERMAN, "MDZ Homepage")
            .lastModified("2019-12-02T12:46:46.6262")
            .refId(29)
            .url("https://www.digitale-sammlungen.de/")
            .rootPages(
                List.of(
                    Webpage.builder()
                        .created("2020-07-07T17:04:52.129368")
                        .label(Locale.GERMAN, "Footer")
                        .lastModified("2020-07-07T17:04:52.129378")
                        .uuid("157f5428-5a5a-4d47-971e-f092f1836246")
                        .publicationStart("2020-07-07")
                        .build(),
                    Webpage.builder()
                        .created("2019-09-09T15:05:17.356311")
                        .label(Locale.GERMAN, "News")
                        .lastModified("2019-09-09T15:05:17.356343")
                        .uuid("692b3792-f5d9-4e6d-9bb6-7fff6345467a")
                        .publicationStart("2019-09-09")
                        .notShownInNavigation()
                        .build(),
                    Webpage.builder()
                        .created("2020-06-30T11:27:57.050954")
                        .description(
                            "de", "technischer Knoten, der alle Webpages des Hauptmenüs enthält")
                        .label(Locale.GERMAN, "Hauptmenü")
                        .lastModified("2020-06-30T11:27:57.050963")
                        .uuid("6d52141c-5c5d-48b4-aee8-7df5404d245e")
                        .publicationStart("2020-06-30")
                        .build(),
                    Webpage.builder()
                        .created("2019-08-19T15:04:29.397957")
                        .label(Locale.GERMAN, "Startseite")
                        .label(Locale.ENGLISH, "MDZ Homepage")
                        .lastModified("2021-03-31T11:36:03.58095")
                        .uuid("76c5d90f-1a72-47ed-819e-f9e86328a304")
                        .publicationStart("2020-10-01")
                        .shownInNavigation()
                        .build()))
            .build();

    when(websiteService.getByUuid(any(UUID.class))).thenReturn(expected);

    testJson(path);
  }
}
