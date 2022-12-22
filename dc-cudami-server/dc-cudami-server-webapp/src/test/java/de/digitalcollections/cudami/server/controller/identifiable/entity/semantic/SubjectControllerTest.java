package de.digitalcollections.cudami.server.controller.identifiable.entity.semantic;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.identifiable.entity.semantic.SubjectService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.semantic.Subject;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

  @DisplayName("can retrieve by localized exact label")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/subjects?label=\"Antike und Altertum\"&labelLanguage=und-Latn"})
  public void findByLocalizedExactLabel(String path) throws Exception {
    testHttpGet(path);
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(25)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("label.und-latn")
                            .isEquals("\"Antike und Altertum\"")
                            .build())
                    .build())
            .build();
    verify(subjectService, times(1)).find(eq(expectedPageRequest));
  }

  @DisplayName("can retrieve by localized 'like' label")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/subjects?label=Antike&labelLanguage=und-Latn"})
  public void findByLocalizedLikeLabel(String path) throws Exception {
    testHttpGet(path);
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(25)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("label.und-latn")
                            .contains("Antike")
                            .build())
                    .build())
            .build();
    verify(subjectService, times(1)).find(eq(expectedPageRequest));
  }
}
