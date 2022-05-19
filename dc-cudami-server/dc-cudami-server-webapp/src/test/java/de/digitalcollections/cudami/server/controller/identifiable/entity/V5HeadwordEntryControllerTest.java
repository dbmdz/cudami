package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.HeadwordEntryService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.HeadwordEntry;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5HeadwordEntryController.class)
@DisplayName("The V5 HeadwordEntry Controller")
class V5HeadwordEntryControllerTest extends BaseControllerTest {

  @MockBean private HeadwordEntryService headwordEntryService;

  @DisplayName("shall return a paged list of headword entries")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/headwordentries?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<HeadwordEntry> expected =
        (PageResponse<HeadwordEntry>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(1365676)
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withoutContent()
                .build();

    when(headwordEntryService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/headwordentries/find_with_empty_result.json");
  }
}
