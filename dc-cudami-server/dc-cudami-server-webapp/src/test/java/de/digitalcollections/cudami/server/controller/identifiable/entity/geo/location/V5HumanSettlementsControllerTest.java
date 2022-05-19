package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.HumanSettlementService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5HumanSettlementsController.class)
@DisplayName("The V5 HumanSettlementsController")
class V5HumanSettlementsControllerTest extends BaseControllerTest {

  @MockBean private HumanSettlementService humanSettlementService;

  @DisplayName("shall return a paged list of human settlements")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/humansettlements?pageSize=1&pageNumber=0",
        "/v2/humansettlements?pageSize=1&pageNumber=0",
        "/latest/humansettlements?pageSize=1&pageNumber=0"
      })
  void testFind(String path) throws Exception {
    PageResponse<HumanSettlement> expected =
        (PageResponse<HumanSettlement>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(0)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .build();

    when(humanSettlementService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/humansettlements/find_with_empty_result.json");
  }
}
