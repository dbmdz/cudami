package de.digitalcollections.cudami.server.controller.semantic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.business.api.service.semantic.TagService;
import de.digitalcollections.cudami.server.controller.BaseControllerTest;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.semantic.Tag;
import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

  @DisplayName("can retrieve by non-encoded value")
  @ParameterizedTest
  @ValueSource(strings = {"/v6/tags/value/foobar"})
  public void findByUnencodedValue(String path) throws Exception {
    when(tagService.getByValue(any())).thenReturn(Tag.builder().build());
    testHttpGet(path);
    verify(tagService, times(1)).getByValue(eq("foobar"));
  }

  @DisplayName("can retrieve by encoded value")
  @Test
  public void findByEncodedValue() throws Exception {
    final String encodedValue =
        Base64.encodeBase64URLSafeString("foobar".getBytes(StandardCharsets.UTF_8));

    when(tagService.getByValue(eq(encodedValue))).thenReturn(null);
    when(tagService.getByValue(eq("foobar"))).thenReturn(Tag.builder().build());
    testHttpGet("/v6/tags/value/" + encodedValue);
    verify(tagService, times(1)).getByValue(eq(encodedValue));
    verify(tagService, times(1)).getByValue(eq("foobar"));
  }
}
