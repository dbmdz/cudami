package de.digitalcollections.cudami.server.controller.identifiable.entity.semantic;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic.SubjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.semantic.Subject;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(SubjectController.class)
@DisplayName("The SubjectController")
class SubjectControllerTest extends BaseControllerTest {

  @MockBean private SubjectService subjectService;

  @DisplayName("shall return a subject by type, namespace and id")
  @Test
  public void returnByTypeNamespaceId() throws Exception {
    String path =
        "/v6/subjects/identifier/"
            + Base64.encodeBase64String("type:namespace:id".getBytes(StandardCharsets.UTF_8));
    Subject expected =
        Subject.builder()
            .identifier(Identifier.builder().namespace("namespace").id("id").build())
            .type("type")
            .build();
    when(subjectService.getByTypeAndIdentifier(eq("type"), eq("namespace"), eq("id")))
        .thenReturn(expected);

    testJson(path, "/v5/subjects/type_namespace_id.json");
  }

  @DisplayName(
      "throws a 404 exception, when a subject could not be found by type, namespace and id")
  @Test
  public void notFound() throws Exception {
    String path =
        "/v6/subjects/identifier/"
            + Base64.encodeBase64String("type:namespace:id".getBytes(StandardCharsets.UTF_8));
    when(subjectService.getByTypeAndIdentifier(eq("type"), eq("namespace"), eq("id")))
        .thenReturn(null);
    testNotFound(path);
  }
}
