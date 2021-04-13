package de.digitalcollections.cudami.server.controller.identifiable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.OrderBuilder;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V2IdentifierTypeController.class)
@DisplayName("The V2IdentifierTypeController")
class V2IdentifierTypeControllerTest extends BaseControllerTest {

  @MockBean private IdentifierTypeService identifierTypeService;

  @DisplayName("returns a list of identifier types")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/identifiertypes?pageNumber=0&pageSize=5"})
  public void identifierTypesList(String path) throws Exception {
    PageResponse<IdentifierType> response = new PageResponse<>();
    IdentifierType identifierType = new IdentifierType();
    identifierType.setLabel("MDZ-ID");
    identifierType.setNamespace("mdz-obj");
    identifierType.setPattern("^bsb[0-9]{8}$");
    identifierType.setUuid(UUID.fromString("d0e7f4b8-7d0c-4233-b58d-20437477672b"));

    List<IdentifierType> identifierTypes = List.of(identifierType);
    response.setContent(identifierTypes);

    PageRequest pageRequest = new PageRequest();
    pageRequest.setPageNumber(0);
    pageRequest.setPageSize(5);
    Sorting sorting = new Sorting();
    Order order =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property("namespace")
            .build();
    List<Order> orders = List.of(order);
    sorting.setOrders(orders);
    pageRequest.setSorting(sorting);
    response.setPageRequest(pageRequest);

    response.setTotalElements(1);

    when(identifierTypeService.find(any(PageRequest.class))).thenReturn(response);

    testJson(path, "/v2/identifiertypes/identifiertypes.json");
  }
}
