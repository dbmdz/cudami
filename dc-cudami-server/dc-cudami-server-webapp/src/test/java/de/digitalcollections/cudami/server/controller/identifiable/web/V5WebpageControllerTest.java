package de.digitalcollections.cudami.server.controller.identifiable.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.LocaleService;
import de.digitalcollections.cudami.server.business.api.service.identifiable.web.WebpageService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.web.Webpage;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5WebpageController.class)
@DisplayName("The V5WebpageController")
class V5WebpageControllerTest extends BaseControllerTest {

  @MockBean private LocaleService localeService;
  @MockBean private WebpageService webpageService;

  @DisplayName("shall return a paged list of webpages ")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/webpages/?pageSize=1&pageNumber=0",
        "/v2/webpages/?pageSize=1&pageNumber=0",
        "/latest/webpages/?pageSize=1&pageNumber=0"
      })
  void testFindAll(String path) throws Exception {
    PageResponse<Webpage> expected =
        (PageResponse<Webpage>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(0)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withoutContent()
                .build();

    when(webpageService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/webpages/find_with_empty_result.json");
  }

  @DisplayName("shall return a paged list of webpages for a parent uuid")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/webpages/599a120c-2dd5-11e8-b467-0ed5f89f718b/children?pageSize=1&pageNumber=0"
      })
  void testFind(String path) throws Exception {
    PageResponse<Webpage> expected =
        (PageResponse<Webpage>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(0)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withoutContent()
                .build();

    when(webpageService.findChildren(any(UUID.class), any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/webpages/599a120c-2dd5-11e8-b467-0ed5f89f718b_children_empty.json");
  }
}
