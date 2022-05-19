package de.digitalcollections.cudami.server.controller.identifiable.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.agent.GivenNameService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.agent.GivenName;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(GivenNameController.class)
@DisplayName("The GivenNameController")
class GivenNameControllerTest extends BaseControllerTest {

  @MockBean private GivenNameService givenNameService;

  @DisplayName("shall return a paged list of given names")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/givennames?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<GivenName> expected =
        (PageResponse<GivenName>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(0)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .build();

    when(givenNameService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v6/givennames/find_with_empty_result.json");
  }
}
