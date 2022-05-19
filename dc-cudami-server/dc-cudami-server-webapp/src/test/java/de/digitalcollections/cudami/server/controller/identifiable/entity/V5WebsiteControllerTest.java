package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.WebsiteService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Website;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5WebsiteController.class)
@DisplayName("The V5 Website Controller")
class V5WebsiteControllerTest extends BaseControllerTest {

  @MockBean private WebsiteService websiteService;

  @DisplayName("shall return a paged list of websites")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/websites?pageSize=1&pageNumber=0",
      })
  void testFind(String path) throws Exception {
    PageResponse<Website> expected =
        (PageResponse<Website>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(82)
                .forDescendingOrderedField("label", "de")
                .withContent(
                    List.of(
                        Website.builder()
                            .label(Locale.GERMAN, "Test-Website")
                            .created("2019-02-14T00:00:00")
                            .lastModified("2019-02-14T00:00:00")
                            .uuid("786f8c74-2a9e-4f74-888f-667522df6538")
                            .refId(59)
                            .url("http://foo.bar")
                            .description("de", "")
                            .build()))
                .build();

    when(websiteService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/websites/find_with_result.json");
  }

  @DisplayName("shall return a paged list of root pages for a website")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/websites/786f8c74-2a9e-4f74-888f-667522df6538/rootpages?pageSize=1&pageNumber=0",
      })
  void testFindRootpages(String path) throws Exception {
    PageResponse<Webpage> expected =
        (PageResponse<Webpage>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(3)
                .forDescendingOrderedField("label", "de")
                .withContent(
                    List.of(
                        Webpage.builder()
                            .created("2018-05-02T00:00:00")
                            .lastModified("2018-05-02T00:00:00")
                            .label(Locale.GERMAN, "Impressum")
                            .uuid("0b591826-6c1e-4e2b-87c3-6fbcdc934934")
                            .publicationStart("2018-07-02")
                            .description(Locale.GERMAN, "")
                            .build()))
                .build();

    when(websiteService.findRootWebpages(any(UUID.class), any(PageRequest.class)))
        .thenReturn(expected);

    testJson(path, "/v5/websites/786f8c74-2a9e-4f74-888f-667522df6538_rootpages.json");
  }
}
