package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.BaseWebpageControllerTest;
import de.digitalcollections.cudami.server.model.WebpageBuilder;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageRequestBuilder;
import de.digitalcollections.model.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V3WebsiteController.class)
@DisplayName("The V3WebsiteController")
public class V3WebsiteControllerTest extends BaseWebpageControllerTest {

  @MockBean private WebsiteService websiteService;

  @DisplayName("returns the paged rootpages")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v3/websites/7a2f1935-c5b8-40fb-8622-c675de0a6242/rootpages?pageNumber=0&pageSize=20"
      })
  public void pagedRootpages(String path) throws Exception {
    PageResponse<Webpage> expected = new PageResponse();
    expected.setTotalElements(4);
    expected.setPageRequest(new PageRequestBuilder().pageSize(25).pageNumber(0).build());
    List<Webpage> rootpages =
        List.of(
            createMetaWebpage(
                "2019-08-19T15:04:29.397957",
                Map.of(Locale.GERMAN, "Startseite", Locale.ENGLISH, "MDZ Homepage"),
                "2021-03-31T11:36:03.58095",
                "76c5d90f-1a72-47ed-819e-f9e86328a304",
                "2020-10-01",
                true),
            createMetaWebpage(
                "2019-09-09T15:05:17.356311",
                Map.of(Locale.GERMAN, "News"),
                "2019-09-09T15:05:17.356343",
                "692b3792-f5d9-4e6d-9bb6-7fff6345467a",
                "2019-09-09",
                false),
            new WebpageBuilder()
                .setCreated("2020-06-30T11:27:57.050954")
                .setSimpleDescription(
                    Map.of("de", "technischer Knoten, der alle Webpages des Hauptmenüs enthält"))
                .setLabel(Map.of("de", "Hauptmenü"))
                .setLastModified("2020-06-30T11:27:57.050963")
                .setUUID("6d52141c-5c5d-48b4-aee8-7df5404d245e")
                .setPublicationStart("2020-06-30")
                .build(),
            createMetaWebpage(
                "2020-07-07T17:04:52.129368",
                Map.of(Locale.GERMAN, "Footer"),
                "2020-07-07T17:04:52.129378",
                "157f5428-5a5a-4d47-971e-f092f1836246",
                "2020-07-07",
                false));
    expected.setContent(rootpages);

    when(websiteService.getRootPages(any(UUID.class), any(PageRequest.class))).thenReturn(expected);

    testJson(path);
  }
}
