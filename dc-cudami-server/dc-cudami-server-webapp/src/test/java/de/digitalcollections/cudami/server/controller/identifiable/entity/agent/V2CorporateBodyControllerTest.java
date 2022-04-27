package de.digitalcollections.cudami.server.controller.identifiable.entity.agent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.agent.CorporateBodyService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V2CorporateBodyController.class)
@DisplayName("The V2 CorporateBodyController")
public class V2CorporateBodyControllerTest extends BaseControllerTest {

  @MockBean private CorporateBodyService corporateBodyService;

  @DisplayName("shall return a list of corporate bodies")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/corporatebodies?pageSize=1"})
  void testFindAll(String path) throws Exception {
    SearchPageResponse<CorporateBody> expected =
        (SearchPageResponse<CorporateBody>)
            SearchPageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(75)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .withContent(
                    CorporateBody.builder()
                        .createdAt("2020-09-30T16:29:58.150463")
                        .withIdentifier("gnd", "4084641-6", "344ac2d7-f5a3-45f4-ae6e-6cdf2adc7e6f")
                        .withLabel(Locale.GERMAN, "Abensberg")
                        .lastModifiedAt("2020-09-30T16:29:58.150464")
                        .withPreviewImage(
                            "Wappen%20von%20Abensberg.svg",
                            "2d67f93a-e12c-416b-8b31-796ec667d561",
                            "https://commons.wikimedia.org/wiki/Special:FilePath/Wappen%20von%20Abensberg.svg?width=270")
                        .withUuid("fa34206a-0f2a-49ae-be42-22d011fc71ff")
                        .withRefId(1300707)
                        .withHomepageUrl("https://www.abensberg.de/")
                        .build())
                .build();

    when(corporateBodyService.find(any(SearchPageRequest.class))).thenReturn(expected);

    testJson(path, "/v2/corporatebodies/corporatebodies.json");
  }
}
