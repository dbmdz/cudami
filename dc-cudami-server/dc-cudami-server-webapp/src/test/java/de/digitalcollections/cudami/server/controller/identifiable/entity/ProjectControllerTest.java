package de.digitalcollections.cudami.server.controller.identifiable.entity;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.EntityType;
import de.digitalcollections.model.identifiable.entity.Project;
import de.digitalcollections.model.text.LocalizedText;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(ProjectController.class)
@DisplayName("The ProjectController")
class ProjectControllerTest extends BaseControllerTest {

  @DisplayName("shall return a project by its uuid")
  @ParameterizedTest
  @ValueSource(strings = {"/v2/projects/ae2a0a61-5255-46d4-8acf-cfddd3527338"})
  public void getProjectForUuid(String path) throws Exception {
    Project project = new Project();
    project.setCreated(LocalDateTime.parse("2020-09-30T16:25:10.609465"));
    Identifier projectIdentifier =
        new Identifier(
            UUID.fromString("ae2a0a61-5255-46d4-8acf-cfddd3527338"), "mdz-proj", "1467037957");
    projectIdentifier.setUuid(UUID.fromString("c938279a-dedb-4531-9e94-55091b8e6f72"));
    project.addIdentifier(projectIdentifier);
    project.setLabel(new LocalizedText(Locale.GERMAN, "100(0) Dokumente"));
    project.setLastModified(LocalDateTime.parse("2021-04-01T04:15:01.406352"));
    project.setType(IdentifiableType.ENTITY);
    project.setUuid(extractFirstUuidFromPath(path));
    project.setEntityType(EntityType.PROJECT);
    project.setRefId(1300623);

    when(projectService.get(eq(project.getUuid()))).thenReturn(project);

    mockMvc
        .perform(get(path))
        .andExpect(status().isOk())
        .andExpect(content().contentType(ContentType.APPLICATION_JSON.getMimeType()))
        .andExpect(content().json(getJsonFromFileResource(path)));
  }
}
