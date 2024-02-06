package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.semantic;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.jdbi.AbstractRepositoryImplTest;
import de.digitalcollections.cudami.server.backend.impl.jdbi.type.DbIdentifierMapper;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.semantic.Subject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.validation.ValidationException;
import java.util.Locale;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {SubjectRepositoryImpl.class})
@DisplayName("The Subject Repository")
class SubjectRepositoryImplTest extends AbstractRepositoryImplTest {

  SubjectRepositoryImpl repo;

  @Autowired DbIdentifierMapper dbIdentifierMapper;

  @BeforeEach
  public void beforeEach() {
    repo = new SubjectRepositoryImpl(jdbi, cudamiConfig, dbIdentifierMapper);
  }

  @DisplayName("can save and retrieve by uuid")
  @Test
  void saveAndRetrieveByUuid() throws RepositoryException, ValidationException {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    final Identifier identifier1 = Identifier.builder().namespace("name,space1").id("id1").build();
    final Identifier identifier2 = Identifier.builder().namespace("namespace2").id("id2").build();
    Subject subject =
        Subject.builder()
            .subjectType("test")
            .label(label)
            .identifier(identifier1)
            .identifier(identifier2)
            .build();

    repo.save(subject);
    assertThat(subject.getUuid()).isNotNull();
    assertThat(subject.getCreated()).isNotNull();
    assertThat(subject.getLastModified()).isNotNull();
    assertThat(subject.getSubjectType()).isEqualTo("test");
    assertThat(subject.getLabel()).isEqualTo(label);
    assertThat(subject.getIdentifiers()).containsExactlyInAnyOrder(identifier1, identifier2);

    Subject retrievedSubject = repo.getByUuid(subject.getUuid());

    assertThat(retrievedSubject).isEqualTo(subject);
  }

  @DisplayName("can save and successfully delete")
  @Test
  void saveAndDelete() throws RepositoryException, ValidationException {
    Subject savedSubject =
        ensureSavedSubject(Locale.GERMAN, "Test", "sbject-namespace", "subject-id2", "type");
    boolean success = repo.deleteByUuid(savedSubject.getUuid());
    assertThat(success).isTrue();

    Subject nonexistingSubject = repo.getByUuid(savedSubject.getUuid());
    assertThat(nonexistingSubject).isNull();

    boolean nonsuccess = repo.deleteByUuid(savedSubject.getUuid()); // second attempt must fail!
    assertThat(nonsuccess).isFalse();
  }

  @DisplayName("can save and update")
  @Test
  void saveAndUpdate() throws RepositoryException, ValidationException {
    Subject subject =
        ensureSavedSubject(Locale.GERMAN, "Test", "subject-namespace", "subject-id3", "type");
    Identifier savedSubjectIdentifier = subject.getIdentifiers().stream().findFirst().orElse(null);

    subject.setLabel(new LocalizedText(Locale.GERMAN, "different label"));
    subject.setIdentifiers(
        Set.of(
            Identifier.builder()
                .namespace(savedSubjectIdentifier.getNamespace())
                .id("subject-id3other")
                .build()));

    Subject beforeUpdate = createDeepCopy(subject);

    repo.update(subject);

    beforeUpdate.setLastModified(subject.getLastModified());
    assertThat(subject).isEqualTo(beforeUpdate);
  }

  @DisplayName("can retrieve all subjects with paging")
  @Test
  void findAllPaged() throws RepositoryException, ValidationException {
    Subject savedSubject =
        ensureSavedSubject(Locale.GERMAN, "Test", "subject-namespace", "subject-id4", "type");

    PageResponse<Subject> pageResponse =
        repo.find(PageRequest.builder().pageNumber(0).pageSize(99).build());
    assertThat(pageResponse.getContent()).containsExactly(savedSubject);
  }

  @DisplayName("can retrieve all subjects with sorting")
  @Test
  void findAllPagedAndSorted() throws RepositoryException, ValidationException {
    Subject savedSubject1 =
        ensureSavedSubject(Locale.GERMAN, "Test", "subject-namespace", "subject-id5b", "type-b");
    Subject savedSubject2 =
        ensureSavedSubject(Locale.GERMAN, "Test", "subject-namespace", "subject-id5a", "type-a");

    PageResponse<Subject> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .sorting(
                    Sorting.builder()
                        .order(
                            Order.builder()
                                .property("subjectType")
                                .direction(Direction.ASC)
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedSubject2, savedSubject1);
  }

  @DisplayName("can retrieve subjects with filtering")
  @Test
  void findFiltered() throws RepositoryException, ValidationException {
    Subject savedSubject =
        ensureSavedSubject(Locale.GERMAN, "Test", "subject-namespace", "subject-id6", "type");

    PageResponse<Subject> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.nativeBuilder()
                                .withExpression("identifier.namespace")
                                .isEquals("subject-namespace")
                                .build())
                        .add(
                            FilterCriterion.nativeBuilder()
                                .withExpression("identifier.id")
                                .isEquals("subject-id6")
                                .build())
                        .add(
                            FilterCriterion.builder()
                                .withExpression("subjectType")
                                .isEquals("type")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedSubject);
  }

  @DisplayName("can return an empty filtered set when no matches are found")
  @Test
  void noMatches() throws RepositoryException, ValidationException {
    Subject savedSubject =
        ensureSavedSubject(Locale.GERMAN, "Test", "subject-namespace", "subject-id7", "type");

    PageResponse<Subject> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.nativeBuilder()
                                .withExpression("identifier.namespace")
                                .isEquals("subject-namespace")
                                .build())
                        .add(
                            FilterCriterion.nativeBuilder()
                                .withExpression("identifier.id")
                                .isEquals("nonexistent")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).isEmpty();
  }

  @DisplayName("can return by type, namespace and id")
  @Test
  void getByTypeAndIdentifier() throws RepositoryException, ValidationException {
    Subject savedSubject =
        ensureSavedSubject(null, null, "subject-namespace", "subject-id8", "type");

    Subject foundSubject = repo.getByTypeAndIdentifier("type", "subject-namespace", "subject-id8");
    assertThat(foundSubject).isEqualTo(savedSubject);
  }

  @DisplayName("can find 'like' by label")
  @Test
  void findByLabel() throws RepositoryException, ValidationException {
    Subject savedSubject =
        ensureSavedSubject(Locale.forLanguageTag("und-Latn"), "Testsubject1", null, null, "type");
    ensureSavedSubject(Locale.GERMAN, "Testsubject2", null, null, "type");

    PageResponse<Subject> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(2)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("label.und-Latn")
                                .contains("Testsubject1")
                                .build())
                        .build())
                .build());

    assertThat(pageResponse.getContent()).containsExactly(savedSubject);
  }

  @DisplayName("can find exact by label")
  @Test
  void findExactByLabel() throws RepositoryException, ValidationException {
    Subject savedSubject =
        ensureSavedSubject(Locale.forLanguageTag("und-Latn"), "Karl Ranseier", null, null, "type");
    ensureSavedSubject(Locale.forLanguageTag("und-Latn"), "Hans Dampf", null, null, "type");

    PageResponse<Subject> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(2)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("label.und-Latn")
                                .isEquals("\"Karl Ranseier\"")
                                .build())
                        .build())
                .build());

    assertThat(pageResponse.getContent()).containsExactly(savedSubject);
  }

  // ------------------------------------------------------

  private Subject ensureSavedSubject(
      Locale labelLocale, String labelText, String namespace, String id, String type)
      throws RepositoryException, ValidationException {
    Subject subject =
        Subject.builder()
            .label(
                labelLocale != null && labelText != null
                    ? new LocalizedText(labelLocale, labelText)
                    : null)
            .subjectType(type)
            .build();

    if ((namespace != null) && (id != null)) {
      subject.setIdentifiers(Set.of(Identifier.builder().namespace(namespace).id(id).build()));
    }

    repo.save(subject);
    return subject;
  }
}
