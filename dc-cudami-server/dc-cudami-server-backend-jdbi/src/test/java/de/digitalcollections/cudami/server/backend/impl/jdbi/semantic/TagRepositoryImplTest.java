package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Tag;
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
    classes = {TagRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The Tag Repository")
class TagRepositoryImplTest {

  TagRepositoryImpl repo;

  @Autowired CudamiConfig cudamiConfig;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo = new TagRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @DisplayName("can save and retrieve by uuid")
  @Test
  void saveAndRetrieveByUuid() {
    Tag tag = Tag.builder().value("foo").build();

    Tag savedTag = repo.save(tag);

    assertThat(savedTag.getValue()).isEqualTo(tag.getValue());
    assertThat(savedTag.getUuid()).isNotNull();
    assertThat(savedTag.getCreated()).isNotNull();
    assertThat(savedTag.getLastModified()).isNotNull();

    Tag retrievedTag = repo.getByUuid(savedTag.getUuid());

    assertThat(retrievedTag).isEqualTo(savedTag);
  }

  @DisplayName("can save and successfully delete")
  @Test
  void saveAndDelete() {
    Tag savedTag = ensureSavedTag("Test");
    boolean success = repo.delete(savedTag.getUuid());
    assertThat(success).isTrue();

    Tag nonexistingTag = repo.getByUuid(savedTag.getUuid());
    assertThat(nonexistingTag).isNull();
  }

  @DisplayName("can save and update")
  @Test
  void saveAndUpdate() {
    Tag savedTag = ensureSavedTag("Test");

    Tag tagToUpdate =
        Tag.builder()
            .value(savedTag.getValue())
            .uuid(savedTag.getUuid())
            .created(savedTag.getCreated())
            .build();

    Tag updatedTag = repo.update(tagToUpdate);

    assertThat(updatedTag).isEqualTo(tagToUpdate);
  }

  @DisplayName("can retrieve all tags with paging")
  @Test
  void findAllPaged() {
    Tag savedTag = ensureSavedTag("Test");

    PageResponse<Tag> pageResponse =
        repo.find(PageRequest.builder().pageNumber(0).pageSize(99).build());
    assertThat(pageResponse.getContent()).containsExactly(savedTag);
  }

  @DisplayName("can retrieve all tags with sorting")
  @Test
  void findAllPagedAndSorted() {
    Tag savedTag1 = ensureSavedTag("Test1");
    Tag savedTag2 = ensureSavedTag("Test2");

    PageResponse<Tag> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .sorting(
                    Sorting.builder()
                        .order(Order.builder().property("value").direction(Direction.ASC).build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedTag1, savedTag2);
  }

  @DisplayName("can retrieve tags with filtering")
  @Test
  void findFiltered() {
    Tag savedTag = ensureSavedTag("Test");

    PageResponse<Tag> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("value")
                                .isEquals("Test")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedTag);
  }

  @DisplayName("can return an empty filtered set when no matches are found")
  @Test
  void noMatches() {
    ensureSavedTag("Test");

    PageResponse<Tag> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("value")
                                .isEquals("nonexistent")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).isEmpty();
  }

  @DisplayName("can return by value")
  @Test
  void getByValue() {
    Tag savedTag = ensureSavedTag("foo");
    Tag foundTag = repo.getByValue("foo");
    assertThat(foundTag).isEqualTo(savedTag);
  }

  @DisplayName("can find 'like' by value")
  @Test
  void findByValue() throws RepositoryException {
    Tag savedTag = ensureSavedTag("Testtag1");
    ensureSavedTag("Testtag2");

    PageResponse<Tag> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(2)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("value")
                                .contains("Testtag1")
                                .build())
                        .build())
                .build());

    assertThat(pageResponse.getContent()).containsExactly(savedTag);
  }

  // ------------------------------------------------------------------------------------------
  private Tag ensureSavedTag(String value) {
    Tag tag = Tag.builder().value(value).build();

    return repo.save(tag);
  }
}
