package de.digitalcollections.cudami.server.controller.identifiable.entity.work;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.work.ItemService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.item.Item;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5ItemController.class)
@DisplayName("The V5 ItemController")
class V5ItemControllerTest extends BaseControllerTest {

  @MockBean private ItemService itemService;

  @DisplayName("shall return a paged list of items")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v5/items?pageNumber=0&pageSize=1&sortBy=label_de.asc",
        "/v2/items?pageNumber=0&pageSize=1&sortBy=label_de.asc",
        "/latest/items?pageNumber=0&pageSize=1&sortBy=label_de.asc"
      })
  public void find(String path) throws Exception {
    PageResponse<Item> expected =
        (PageResponse<Item>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(0)
                .forAscendingOrderedField("label", "de")
                .withoutContent()
                .build();

    when(itemService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/items/find_with_empty_result.json");
  }
}
