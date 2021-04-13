package de.digitalcollections.cudami.server.controller.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.NullHandling;
import de.digitalcollections.model.paging.Order;
import de.digitalcollections.model.paging.OrderBuilder;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V3RenderingTemplateController.class)
@DisplayName("The V3RenderingTemplateController")
class V3RenderingTemplateControllerTest extends BaseControllerTest {

  @MockBean private RenderingTemplateService renderingTemplateService;

  @DisplayName("returns a list of rendering templates")
  @ParameterizedTest
  @ValueSource(
      strings = {"/v3/renderingtemplates?pageNumber=0&pageSize=1&sortBy=label_de.asc,name.asc"})
  public void renderingTemplatesList(String path) throws Exception {
    PageResponse<RenderingTemplate> response = new PageResponse<>();
    RenderingTemplate renderingTemplate = new RenderingTemplate();
    renderingTemplate.setDescription(
        new LocalizedText(Locale.GERMAN, "Template für ein Akkordion (z.B. FAQs)"));
    renderingTemplate.setLabel(new LocalizedText(Locale.GERMAN, "Akkordeon"));
    renderingTemplate.setName("accordion");
    renderingTemplate.setUuid(UUID.fromString("ba62495c-fb69-4d4a-9ca0-19e106a11aa7"));

    List<RenderingTemplate> renderingTemplates = List.of(renderingTemplate);
    response.setContent(renderingTemplates);

    PageRequest pageRequest = new PageRequest();
    pageRequest.setPageNumber(0);
    pageRequest.setPageSize(1);
    Sorting sorting = new Sorting();
    Order order1 =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property("label")
            .subProperty("de")
            .build();
    Order order2 =
        new OrderBuilder()
            .direction(Direction.ASC)
            .ignoreCase(false)
            .nullHandling(NullHandling.NATIVE)
            .property("name")
            .build();
    List<Order> orders = List.of(order1, order2);
    sorting.setOrders(orders);
    pageRequest.setSorting(sorting);
    response.setPageRequest(pageRequest);
    response.setTotalElements(2);

    when(renderingTemplateService.find(any(PageRequest.class))).thenReturn(response);

    testJson(path);
  }
}
