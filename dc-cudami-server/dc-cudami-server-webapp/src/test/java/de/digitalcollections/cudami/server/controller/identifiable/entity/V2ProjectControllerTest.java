package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.cudami.server.model.ProjectBuilder;
import de.digitalcollections.cudami.server.model.SearchPageResponseBuilder;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
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

  @DisplayName("shall return a paged project list")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/projects/?pageNumber=0&pageSize=1"})
  public void projectList(String path) throws Exception {
    SearchPageResponse<Project> expected =
        (SearchPageResponse)
            new SearchPageResponseBuilder()
                .forPageSize(1)
                .withTotalElements(395)
                .withContent(
                    new ProjectBuilder()
                        .createdAt("2020-09-30T16:25:10.609465")
                        .withIdentifier(
                            "mdz-proj", "1467037957", "898947e9-0d61-4572-b87e-05a01868001d")
                        .withLabel(Locale.GERMAN, "100(0) Dokumente")
                        .lastModifiedAt("2021-04-13T04:15:01.274821")
                        .withUuid("ae2a0a61-5255-46d4-8acf-cfddd3527338")
                        .withRefId(1300623)
                        .build())
                .forAscendingOrderedField("label", "de")
                .forAscendingOrderedField("label")
                .build();

    when(projectService.find(any(SearchPageRequest.class))).thenReturn(expected);

    testJson(path, "/v2/projects/projects.json");
  }

  @DisplayName("shall return a project by its uuid")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/projects/ae2a0a61-5255-46d4-8acf-cfddd3527338"})
  public void getProjectForUuid(String path) throws Exception {
    Project expected =
        new ProjectBuilder()
            .createdAt("2020-09-30T16:25:10.609465")
            .withIdentifier("mdz-proj", "1467037957", "c938279a-dedb-4531-9e94-55091b8e6f72")
            .withLabel(Locale.GERMAN, "100(0) Dokumente")
            .lastModifiedAt("2021-04-01T04:15:01.406352")
            .withUuidFromPath(path)
            .withRefId(1300623)
            .build();

    when(projectService.get(eq(expected.getUuid()))).thenReturn(expected);

    testJson(path);
  }
}
