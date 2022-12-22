package de.digitalcollections.cudami.server.business.impl.service.semantic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.semantic.TagRepository;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.semantic.Subject;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The TagService")
class TagServiceImplTest {

  private TagServiceImpl tagService;
  private TagRepository tagRepository;

  @BeforeEach
  public void beforeEach() {
    tagRepository = mock(TagRepository.class);
    tagService = new TagServiceImpl(tagRepository);
  }

  @DisplayName("can find tags")
  @Test
  public void find() {
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
    PageResponse<Tag> expectedPageResponse =
        PageResponse.builder()
            .withContent(
                List.of(
                    Subject.builder()
                        .label(
                            new LocalizedText(
                                Locale.forLanguageTag("und-Latn"), "Antike und Altertum"))
                        .build()))
            .build();
    when(tagRepository.find(eq(expectedPageRequest))).thenReturn(expectedPageResponse);

    PageResponse<Tag> actual = tagService.find(expectedPageRequest);

    assertThat(actual).isEqualTo(expectedPageResponse);
  }
}
