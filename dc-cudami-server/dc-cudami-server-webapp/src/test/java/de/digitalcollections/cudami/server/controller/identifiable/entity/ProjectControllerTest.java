package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.ProjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.Project;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
            .identifier(
                Identifier.builder()
                    .namespace("mdz-proj")
                    .id("1467037957")
                    .uuid("c938279a-dedb-4531-9e94-55091b8e6f72")
                    .build())
            .label(Locale.GERMAN, "100(0) Dokumente")
            .lastModified("2021-04-01T04:15:01.406352")
            .uuid(extractFirstUuidFromPath(path))
            .refId(1300623)
            .build();

    when(projectService.getByExample(any(Project.class))).thenReturn(expected);

    testJson(path);
  }

  @DisplayName("can retrieve by identifier with plaintext id")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/projects/identifier/foo:bar",
        "/v5/projects/identifier/foo:bar",
        "/v3/projects/identifier/foo:bar",
        "/latest/projects/identifier/foo:bar",
        "/v6/projects/identifier/foo:bar.json",
        "/v5/projects/identifier/foo:bar.json",
        "/v3/projects/identifier/foo:bar.json",
        "/latest/projects/identifier/foo:bar.json"
      })
  void testGetByIdentifierWithPlaintextId(String path) throws Exception {
    Project expected = Project.builder().build();

    when(projectService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar").build())))
        .thenReturn(expected);

    testHttpGet(path);

    verify(projectService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar").build()));
  }

  @DisplayName("can retrieve by identifier with base 64 encoded data")
  @ParameterizedTest
  @ValueSource(
      strings = {
        "/v6/projects/identifier/",
        "/v5/projects/identifier/",
        "/v3/projects/identifier/",
        "/latest/projects/identifier/"
      })
  void testGetByIdentifierWithBase64EncodedData(String basePath) throws Exception {
    Project expected = Project.builder().build();

    when(projectService.getByIdentifier(
            eq(Identifier.builder().namespace("foo").id("bar/bla").build())))
        .thenReturn(expected);

    testHttpGet(
        basePath
            + Base64.getEncoder().encodeToString("foo:bar/bla".getBytes(StandardCharsets.UTF_8)));

    verify(projectService, times(1))
        .getByIdentifier(eq(Identifier.builder().namespace("foo").id("bar/bla").build()));
  }
}
