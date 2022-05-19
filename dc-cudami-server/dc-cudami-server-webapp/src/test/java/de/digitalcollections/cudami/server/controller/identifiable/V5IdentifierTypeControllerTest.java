package de.digitalcollections.cudami.server.controller.identifiable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.IdentifierTypeService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.IdentifierType;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5IdentifierTypeController.class)
@DisplayName("The V5IdentifierTypeController")
class V5IdentifierTypeControllerTest extends BaseControllerTest {

  @MockBean private IdentifierTypeService identifierTypeService;

  @DisplayName("returns a paged list of identifier types")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/identifiertypes?pageNumber=0&pageSize=1"})
  public void identifierTypesList(String path) throws Exception {
    PageResponse<IdentifierType> expected =
        (PageResponse)
            PageResponse.builder()
                .forPageSize(1)
                .forRequestPage(0)
                .forAscendingOrderedField("namespace")
                .forAscendingOrderedField("uuid")
                .withContent(
                    IdentifierType.builder()
                        .label("MDZ-ID")
                        .namespace("mdz-obj")
                        .pattern("^bsb[0-9]{8}$")
                        .uuid("d0e7f4b8-7d0c-4233-b58d-20437477672b")
                        .build())
                .build();

    when(identifierTypeService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/identifiertypes/find_with_result.json");
  }
}
