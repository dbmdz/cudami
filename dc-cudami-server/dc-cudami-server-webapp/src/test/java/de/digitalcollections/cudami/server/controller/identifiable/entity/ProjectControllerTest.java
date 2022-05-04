package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.entity.Project;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(ProjectController.class)
@DisplayName("The ProjectController")
class ProjectControllerTest extends BaseControllerTest {

  @MockBean private ProjectService projectService;

  @DisplayName("shall return a project by its uuid")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/projects/ae2a0a61-5255-46d4-8acf-cfddd3527338"})
  public void getProjectForUuid(String path) throws Exception {
    Project expected =
        Project.builder()
            .created("2020-09-30T16:25:10.609465")
            .identifier("mdz-proj", "1467037957", "c938279a-dedb-4531-9e94-55091b8e6f72")
            .label(Locale.GERMAN, "100(0) Dokumente")
            .lastModified("2021-04-01T04:15:01.406352")
            .uuid(extractFirstUuidFromPath(path))
            .refId(1300623)
            .build();

    when(projectService.getByUuid(eq(expected.getUuid()))).thenReturn(expected);

    testJson(path);
  }
}
