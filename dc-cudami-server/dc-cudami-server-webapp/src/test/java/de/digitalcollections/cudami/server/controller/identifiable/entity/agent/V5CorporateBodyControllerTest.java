package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.CorporateBodyService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5CorporateBodyController.class)
@DisplayName("The V5 CorporateBodyController")
class V5CorporateBodyControllerTest extends BaseControllerTest {

  @MockBean private CorporateBodyService corporateBodyService;

  @DisplayName("shall return a paged list of corporate bodies")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/corporatebodies?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<CorporateBody> expected =
        (PageResponse<CorporateBody>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(75)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withContent(
                    CorporateBody.builder()
                        .created("2020-09-30T16:29:58.150463")
                        .label(Locale.GERMAN, "Bayerische Staatsbibliothek")
                        .lastModified("2020-09-30T16:29:58.150464")
                        .uuid("fdfaa0d2-0c62-4ffa-995b-5386aea720be")
                        .refId(1300707)
                        .build())
                .build();

    when(corporateBodyService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/corporatebodies/find_with_result.json");
  }
}
