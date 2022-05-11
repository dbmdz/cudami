package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(V2ProjectController.class)
@DisplayName("The V2 ProjectController")
public class V2ProjectControllerTest extends BaseControllerTest {

  @MockBean private ProjectService projectService;

  @DisplayName("shall return a pages project list")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/projects/?pageNumber=0&pageSize=1"})
  public void projectList(String path) throws Exception {
    PageResponse<Project> expected =
        (PageResponse)
            PageResponse.builder()
                .forPageSize(1)
                .withTotalElements(395)
                .withContent(
                    Project.builder()
                        .created("2020-09-30T16:25:10.609465")
                        .identifier(
                            "mdz-proj", "1467037957", "898947e9-0d61-4572-b87e-05a01868001d")
                        .label(Locale.GERMAN, "100(0) Dokumente")
                        .lastModified("2021-04-13T04:15:01.274821")
                        .uuid("ae2a0a61-5255-46d4-8acf-cfddd3527338")
                        .refId(1300623)
                        .build())
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .build();

    when(projectService.find(any(PageRequest.class))).thenReturn(expected);

    testJson(path, "/v2/projects/projects.json");
  }
}
