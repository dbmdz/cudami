package de.digitalcollections.cudami.server.controller.semantic;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.digitalcollections.cudami.server.business.api.service.semantic.TagService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(TagController.class)
@DisplayName("The TagController")
class TagControllerTest extends BaseControllerTest {

  @MockBean private TagService tagService;

  @DisplayName("can retrieve by localized exact label")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/tags?label=\"Antike und Altertum\"&labelLanguage=und-Latn"})
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
    verify(tagService, times(1)).find(eq(expectedPageRequest));
  }

  @DisplayName("can retrieve by localized 'like' label")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/tags?label=Antike&labelLanguage=und-Latn"})
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
    verify(tagService, times(1)).find(eq(expectedPageRequest));
  }
}
