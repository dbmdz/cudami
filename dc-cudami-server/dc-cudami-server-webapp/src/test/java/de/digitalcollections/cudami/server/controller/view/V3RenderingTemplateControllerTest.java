package de.digitalcollections.cudami.server.controller.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.view.RenderingTemplateService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.paging.PageRequest;
import de.digitalcollections.model.paging.PageResponse;
import de.digitalcollections.model.view.RenderingTemplate;
import java.util.Locale;
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
    PageResponse<RenderingTemplate> expected =
        (PageResponse)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("name")
                .withTotalElements(2)
                .withContent(
                    RenderingTemplate.builder()
                        .name("accordion")
                        .uuid("ba62495c-fb69-4d4a-9ca0-19e106a11aa7")
                        .description(Locale.GERMAN, "Template für ein Akkordion (z.B. FAQs)")
                        .label(Locale.GERMAN, "Akkordeon")
                        .build())
                .build();

    when(renderingTemplateService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path);
  }
}
