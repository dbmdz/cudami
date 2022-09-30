package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.semantic;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.semantic.Subject;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
import java.util.Set;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {SubjectRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The Subject Repository")
class SubjectRepositoryImplTest {

  SubjectRepositoryImpl repo;

  @Autowired CudamiConfig cudamiConfig;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo = new SubjectRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @DisplayName("can save and retrieve by uuid")
  @Test
  void saveAndRetrieveByUuid() {
    final LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    final Identifier identifier1 = Identifier.builder().namespace("name,space1").id("id1").build();
    final Identifier identifier2 = Identifier.builder().namespace("namespace2").id("id2").build();
    Subject subject =
        Subject.builder()
            .type("test")
            .label(label)
            .identifier(identifier1)
            .identifier(identifier2)
            .build();

    Subject savedSubject = repo.save(subject);
    assertThat(savedSubject.getUuid()).isNotNull();
    assertThat(savedSubject.getCreated()).isNotNull();
    assertThat(savedSubject.getLastModified()).isNotNull();
    assertThat(savedSubject.getType()).isEqualTo("test");
    assertThat(savedSubject.getLabel()).isEqualTo(label);
    assertThat(savedSubject.getIdentifiers()).containsExactlyInAnyOrder(identifier1, identifier2);

    Subject retrievedSubject = repo.getByUuid(savedSubject.getUuid());

    assertThat(retrievedSubject).isEqualTo(savedSubject);
  }

  @DisplayName("can save and successfully delete")
  @Test
  void saveAndDelete() {
    Subject savedSubject =
        ensureSavedSubject(Locale.GERMAN, "Test", "sbject-namespace", "subject-id2", "type");
    boolean success = repo.delete(savedSubject.getUuid());
    assertThat(success).isTrue();

    Subject nonexistingSubject = repo.getByUuid(savedSubject.getUuid());
    assertThat(nonexistingSubject).isNull();
  }

  @DisplayName("can save and update")
  @Test
  void saveAndUpdate() {
    Subject savedSubject =
        ensureSavedSubject(Locale.GERMAN, "Test", "subject-namespace", "subject-id3", "type");
    Identifier savedSubjectIdentifier =
        savedSubject.getIdentifiers().stream().findFirst().orElse(null);

    Subject subjectToUpdate =
        Subject.builder()
            .label(new LocalizedText(Locale.GERMAN, "different label"))
            .type(savedSubject.getType())
            .uuid(savedSubject.getUuid())
            .created(savedSubject.getCreated())
            .identifiers(
                Set.of(
                    Identifier.builder()
                        .namespace(savedSubjectIdentifier.getNamespace())
                        .id("subject-id3other")
                        .build()))
            .build();

    Subject updatedSubject = repo.update(subjectToUpdate);

    assertThat(updatedSubject).isEqualTo(subjectToUpdate);
  }

  @DisplayName("can retrieve all subjects with paging")
  @Test
  void findAllPaged() {
    Subject savedSubject =
        ensureSavedSubject(Locale.GERMAN, "Test", "subject-namespace", "subject-id4", "type");

    PageResponse<Subject> pageResponse =
        repo.find(PageRequest.builder().pageNumber(0).pageSize(99).build());
    assertThat(pageResponse.getContent()).containsExactly(savedSubject);
  }

  @DisplayName("can retrieve all subjects with sorting")
  @Test
  void findAllPagedAndSorted() {
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
                        .order(Order.builder().property("type").direction(Direction.ASC).build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedSubject2, savedSubject1);
  }

  @DisplayName("can retrieve subjects with filtering")
  @Test
  void findFiltered() {
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
                            FilterCriterion.builder()
                                .withExpression("identifiers_namespace")
                                .isEquals("subject-namespace")
                                .build())
                        .add(
                            FilterCriterion.builder()
                                .withExpression("identifiers_id")
                                .isEquals("subject-id6")
                                .build())
                        .add(
                            FilterCriterion.builder()
                                .withExpression("type")
                                .isEquals("type")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedSubject);
  }

  @DisplayName("can return an empty filtered set when no matches are found")
  @Test
  void noMatches() {
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
                            FilterCriterion.builder()
                                .withExpression("identifiers_namespace")
                                .isEquals("subject-namespace")
                                .build())
                        .add(
                            FilterCriterion.builder()
                                .withExpression("identifiers_id")
                                .isEquals("nonexistent")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).isEmpty();
  }

  @DisplayName("can return by type, namespace and id")
  @Test
  void getByTypeAndIdentifier() {
    Subject savedSubject =
        ensureSavedSubject(null, null, "subject-namespace", "subject-id8", "type");

    Subject foundSubject = repo.getByTypeAndIdentifier("type", "subject-namespace", "subject-id8");
    assertThat(foundSubject).isEqualTo(savedSubject);
  }

  // ------------------------------------------------------

  private Subject ensureSavedSubject(
      Locale labelLocale, String labelText, String namespace, String id, String type) {
    Subject subject =
        Subject.builder()
            .label(
                labelLocale != null && labelText != null
                    ? new LocalizedText(labelLocale, labelText)
                    : null)
            .identifier(Identifier.builder().namespace(namespace).id(id).build())
            .type(type)
            .build();

    return repo.save(subject);
  }
}
