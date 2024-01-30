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
import de.digitalcollections.model.validation.ValidationException;
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

  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo = new TagRepositoryImpl(jdbi, cudamiConfig);
  }

  @DisplayName("can save and retrieve by uuid")
  @Test
  void saveAndRetrieveByUuid() throws RepositoryException, ValidationException {
    Tag tag = Tag.builder().value("foo").build();
    repo.save(tag);

    assertThat(tag.getValue()).isEqualTo(tag.getValue());
    assertThat(tag.getUuid()).isNotNull();
    assertThat(tag.getCreated()).isNotNull();
    assertThat(tag.getLastModified()).isNotNull();

    Tag retrievedTag = repo.getByUuid(tag.getUuid());

    assertThat(retrievedTag).isEqualTo(tag);
  }

  @DisplayName("can save and successfully delete")
  @Test
  void saveAndDelete() throws RepositoryException, ValidationException {
    Tag savedTag = ensureSavedTag("Test");
    boolean success = repo.deleteByUuid(savedTag.getUuid());
    assertThat(success).isTrue();

    Tag nonexistingTag = repo.getByUuid(savedTag.getUuid());
    assertThat(nonexistingTag).isNull();
  }

  @DisplayName("can save and update")
  @Test
  void saveAndUpdate() throws RepositoryException, ValidationException {
    Tag savedTag = ensureSavedTag("Test");

    Tag tagToUpdate =
        Tag.builder()
            .value(savedTag.getValue())
            .uuid(savedTag.getUuid())
            .created(savedTag.getCreated())
            .build();
    repo.update(tagToUpdate);

    assertThat(savedTag.equals(tagToUpdate));
  }

  @DisplayName("can retrieve all tags with paging")
  @Test
  void findAllPaged() throws RepositoryException, ValidationException {
    Tag savedTag = ensureSavedTag("Test");

    PageResponse<Tag> pageResponse =
        repo.find(PageRequest.builder().pageNumber(0).pageSize(99).build());
    assertThat(pageResponse.getContent()).containsExactly(savedTag);
  }

  @DisplayName("can retrieve all tags with sorting")
  @Test
  void findAllPagedAndSorted() throws RepositoryException, ValidationException {
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
  void findFiltered() throws RepositoryException, ValidationException {
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
  void noMatches() throws RepositoryException, ValidationException {
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
  void getByValue() throws RepositoryException, ValidationException {
    Tag savedTag = ensureSavedTag("foo");
    Tag foundTag = repo.getByValue("foo");
    assertThat(foundTag).isEqualTo(savedTag);
  }

  @DisplayName("can find 'like' by value")
  @Test
  void findByValue() throws RepositoryException, ValidationException {
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
  private Tag ensureSavedTag(String value) throws RepositoryException, ValidationException {
    Tag tag = Tag.builder().value(value).build();
    repo.save(tag);
    return tag;
  }
}
