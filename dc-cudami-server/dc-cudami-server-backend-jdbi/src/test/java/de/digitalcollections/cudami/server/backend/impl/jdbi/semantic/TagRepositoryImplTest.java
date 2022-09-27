package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.semantic.Tag;
import de.digitalcollections.model.text.LocalizedText;
import java.util.Locale;
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
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
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
    LocalizedText label = new LocalizedText(Locale.GERMAN, "Test");
    Tag tag =
        Tag.builder().label(label).namespace("tag-namespace").id("tag-id").type("type").build();

    Tag savedTag = repo.save(tag);

    assertThat(savedTag.getNamespace()).isEqualTo(tag.getNamespace());
    assertThat(savedTag.getId()).isEqualTo(tag.getId());
    assertThat(savedTag.getLabel()).isEqualTo(label);
    assertThat(savedTag.getType()).isEqualTo(tag.getType());
    assertThat(savedTag.getUuid()).isNotNull();
    assertThat(savedTag.getCreated()).isNotNull();
    assertThat(savedTag.getLastModified()).isNotNull();

    Tag retrievedTag = repo.getByUuid(savedTag.getUuid());

    assertThat(retrievedTag).isEqualTo(savedTag);
  }

  @DisplayName("can save and successfully delete")
  @Test
  void saveAndDelete() {
    Tag savedTag = ensureSavedTag(Locale.GERMAN, "Test", "tag-namespace", "tag-id2", "type");
    boolean success = repo.delete(savedTag.getUuid());
    assertThat(success).isTrue();

    Tag nonexistingTag = repo.getByUuid(savedTag.getUuid());
    assertThat(nonexistingTag).isNull();
  }

  @DisplayName("can save and update")
  @Test
  void saveAndUpdate() {
    Tag savedTag = ensureSavedTag(Locale.GERMAN, "Test", "tag-namespace", "tag-id3", "type");

    Tag tagToUpdate =
        Tag.builder()
            .label(new LocalizedText(Locale.GERMAN, "different label"))
            .namespace(savedTag.getNamespace())
            .id(savedTag.getId())
            .type(savedTag.getType())
            .uuid(savedTag.getUuid())
            .created(savedTag.getCreated())
            .build();

    Tag updatedTag = repo.update(tagToUpdate);

    assertThat(updatedTag).isEqualTo(tagToUpdate);
  }

  @DisplayName("can retrieve all tags with paging")
  @Test
  void findAllPaged() {
    Tag savedTag = ensureSavedTag(Locale.GERMAN, "Test", "tag-namespace", "tag-id4", "type");

    PageResponse<Tag> pageResponse =
        repo.find(PageRequest.builder().pageNumber(0).pageSize(99).build());
    assertThat(pageResponse.getContent()).containsExactly(savedTag);
  }

  @DisplayName("can retrieve all tags with sorting")
  @Test
  void findAllPagedAndSorted() {
    Tag savedTag1 = ensureSavedTag(Locale.GERMAN, "Test", "tag-namespace", "tag-id5b", "type");
    Tag savedTag2 = ensureSavedTag(Locale.GERMAN, "Test", "tag-namespace", "tag-id5a", "type");

    PageResponse<Tag> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .sorting(
                    Sorting.builder()
                        .order(Order.builder().property("id").direction(Direction.ASC).build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedTag2, savedTag1);
  }

  @DisplayName("can retrieve tags with filtering")
  @Test
  void findFiltered() {
    Tag savedTag = ensureSavedTag(Locale.GERMAN, "Test", "tag-namespace", "tag-id6", "type");

    PageResponse<Tag> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("namespace")
                                .isEquals("tag-namespace")
                                .build())
                        .add(
                            FilterCriterion.builder()
                                .withExpression("id")
                                .isEquals("tag-id6")
                                .build())
                        .add(
                            FilterCriterion.builder()
                                .withExpression("type")
                                .isEquals("type")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedTag);
  }

  @DisplayName("can return an empty filtered set when no matches are found")
  @Test
  void noMatches() {
    Tag savedTag = ensureSavedTag(Locale.GERMAN, "Test", "tag-namespace", "tag-id7", "type");

    PageResponse<Tag> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("namespace")
                                .isEquals("tag-namespace")
                                .build())
                        .add(
                            FilterCriterion.builder()
                                .withExpression("id")
                                .isEquals("nonexistent")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).isEmpty();
  }

  @DisplayName("can return by type, namespace and id")
  @Test
  void getByTypeAndIdentifier() {
    Tag savedTag = ensureSavedTag(null, null, "tag-namespace", "tag-id8", "type");

    Tag foundTag = repo.getByTypeAndIdentifier("type", "tag-namespace", "tag-id8");
    assertThat(foundTag).isEqualTo(savedTag);
  }

  // ------------------------------------------------------------------------------------------
  private Tag ensureSavedTag(
      Locale labelLocale, String labelText, String namespace, String id, String type) {
    Tag tag =
        Tag.builder()
            .label(
                labelLocale != null && labelText != null
                    ? new LocalizedText(labelLocale, labelText)
                    : null)
            .namespace(namespace)
            .id(id)
            .type(type)
            .build();

    return repo.save(tag);
  }
}
