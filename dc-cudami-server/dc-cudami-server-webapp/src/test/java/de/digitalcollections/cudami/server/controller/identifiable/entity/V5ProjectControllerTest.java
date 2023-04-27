package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V5ProjectController.class)
@DisplayName("The V5 Project Controller")
class V5ProjectControllerTest extends BaseControllerTest {

  @MockBean private ProjectService projectService;

  @DisplayName("shall return a paged list of headword entries")
  @ParameterizedTest
  @ValueSource(strings = {"/v5/projects?pageSize=1&pageNumber=0"})
  void testFind(String path) throws Exception {
    PageResponse<Project> expected =
        (PageResponse<Project>)
            PageResponse.builder()
                .forRequestPage(0)
                .forPageSize(1)
                .withTotalElements(399)
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .forAscendingOrderedField("uuid")
                .withContent(
                    List.of(
                        Project.builder()
                            .label(Locale.GERMAN, "Testprojekt")
                            .uuid("cfa1af83-1d6e-4eed-b36c-75c10b255a21")
                            .refId(590572)
                            .created("2020-10-15T00:00:00")
                            .lastModified("2020-10-15T00:00:00")
                            .identifier(
                                Identifier.builder()
                                    .namespace("mdz-proj")
                                    .id("1467037957")
                                    .uuid("897e213b-f86a-480f-a372-7dbffd906e38")
                                    .build())
                            .build()))
                .build();

    when(projectService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v5/projects/find_with_result.json");
  }
}
