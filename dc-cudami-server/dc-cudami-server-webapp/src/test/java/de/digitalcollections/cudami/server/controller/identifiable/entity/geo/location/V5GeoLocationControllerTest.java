package de.digitalcollections.cudami.server.controller.identifiable.entity.geo.location;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.geo.location.GeoLocationService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.geo.location.GeoLocation;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5GeoLocationController.class)
@DisplayName("The V5 GeoLocationController")
class V5GeoLocationControllerTest extends BaseControllerTest {

  @MockBean private GeoLocationService geoLocationService;

  @DisplayName("shall return a paged list of geolocations")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/geolocations?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<GeoLocation> expected =
        (PageResponse<GeoLocation>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(0)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .build();

    when(geoLocationService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/geolocations/find_with_empty_result.json");
  }
}
