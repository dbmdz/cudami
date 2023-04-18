package de.digitalcollections.cudami.server.business.impl.service.identifiable.entity.semantic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.semantic.SubjectRepository;
import de.digitalcollections.cudami.server.business.api.service.exceptions.ServiceException;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("The SubjectService")
class SubjectServiceImplTest {

  private SubjectServiceImpl subjectService;
  private SubjectRepository subjectRepository;

  @BeforeEach
  public void beforeEach() {
    subjectRepository = mock(SubjectRepository.class);
    subjectService = new SubjectServiceImpl(subjectRepository);
  }

  @DisplayName(
      "throws a ServiceException at getByTypeAndIdentifier when an exception happens in the repository")
  @Test
  public void testCudamiServiceExceptionAtGetByTypeAndIdentifier() throws RepositoryException {
    when(subjectRepository.getByTypeAndIdentifier(any(), any()))
        .thenThrow(new RepositoryException("boo"));

    assertThrows(
        ServiceException.class,
        () ->
            subjectService.getByTypeAndIdentifier(
                "type", Identifier.builder().namespace("namespace").id("id").build()));
  }

  @DisplayName("can find exact subjects")
  @Test
  public void findExact() throws RepositoryException, ServiceException {
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
    PageResponse<Subject> expectedPageResponse =
        PageResponse.builder()
            .withContent(
                List.of(
                    Subject.builder()
                        .label(
                            new LocalizedText(
                                Locale.forLanguageTag("und-Latn"), "Antike und Altertum"))
                        .build()))
            .build();
    when(subjectRepository.find(eq(expectedPageRequest))).thenReturn(expectedPageResponse);

    PageResponse<Subject> actual = subjectService.find(expectedPageRequest);

    assertThat(actual).isEqualTo(expectedPageResponse);
  }

  @DisplayName("can find 'like' subjects")
  @Test
  public void findLike() throws RepositoryException, ServiceException {
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(25)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("label.und-latn")
                            .contains("Antike und Altertum")
                            .build())
                    .build())
            .build();
    PageResponse<Subject> expectedPageResponse =
        PageResponse.builder()
            .withContent(
                List.of(
                    Subject.builder()
                        .label(
                            new LocalizedText(
                                Locale.forLanguageTag("und-Latn"), "Antike und Altertum"))
                        .build(),
                    Subject.builder()
                        .label(new LocalizedText(Locale.forLanguageTag("de"), "Altertum"))
                        .build()))
            .build();
    when(subjectRepository.find(eq(expectedPageRequest))).thenReturn(expectedPageResponse);

    PageResponse<Subject> actual = subjectService.find(expectedPageRequest);

    // FIXME: reactivate following code and fix compare isEqualTo...
    //  PageResponse<Subject> expectedPageResponse =
    //  PageResponse.builder()
    //      .withContent(
    //          List.of(
    //              Subject.builder()
    //                  .label(
    //                      new LocalizedText(
    //                          Locale.forLanguageTag("und-Latn"), "Antike und Altertum"))
    //                  .build()))
    //      .build();

    assertThat(actual).isEqualTo(expectedPageResponse);
  }

  @DisplayName("can find 'like' subjects with partially matching contents")
  @Test
  public void findLikeWithPartiallyMatchingContents() throws RepositoryException, ServiceException {
    PageRequest expectedPageRequest =
        PageRequest.builder()
            .pageSize(25)
            .pageNumber(0)
            .filtering(
                Filtering.builder()
                    .add(
                        FilterCriterion.builder()
                            .withExpression("label.und-latn")
                            .contains("Altertum")
                            .build())
                    .build())
            .build();
    PageResponse<Subject> pageResponse =
        PageResponse.builder()
            .withContent(
                List.of(
                    Subject.builder()
                        .label(
                            new LocalizedText(
                                Locale.forLanguageTag("und-Latn"), "Antike und Altertum"))
                        .build(),
                    Subject.builder()
                        .label(new LocalizedText(Locale.forLanguageTag("de"), "Altertum"))
                        .build()))
            .build();
    when(subjectRepository.find(eq(expectedPageRequest))).thenReturn(pageResponse);

    PageResponse<Subject> actual = subjectService.find(expectedPageRequest);

    // FIXME: reactivate following code and fix compare isEqualTo...
    //    PageResponse<Subject> expectedPageResponse =
    //        PageResponse.builder()
    //            .withContent(
    //                List.of(
    //                    Subject.builder()
    //                        .label(
    //                            new LocalizedText(
    //                                Locale.forLanguageTag("und-Latn"), "Antike und Altertum"))
    //                        .build()))
    //            .build();
    //
    //    assertThat(actual).isEqualTo(expectedPageResponse);
    assertThat(actual).isEqualTo(pageResponse);
  }
}
