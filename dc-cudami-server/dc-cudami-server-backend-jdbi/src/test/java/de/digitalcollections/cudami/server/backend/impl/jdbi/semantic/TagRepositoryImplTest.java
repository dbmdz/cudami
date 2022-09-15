package de.digitalcollections.cudami.server.backend.impl.jdbi.semantic;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
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
    classes = {HeadwordRepositoryImpl.class})
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
    Tag tag = Tag.builder().namespace("tag-namespace").id("tag-id").tagType("tag-type").build();

    Tag savedTag = repo.save(tag);

    assertThat(savedTag.getNamespace()).isEqualTo(tag.getNamespace());
    assertThat(savedTag.getId()).isEqualTo(tag.getId());
    assertThat(savedTag.getTagType()).isEqualTo(tag.getTagType());
    assertThat(savedTag.getUuid()).isNotNull();
    assertThat(savedTag.getCreated()).isNotNull();
    assertThat(savedTag.getLastModified()).isNotNull();

    Tag retrievedTag = repo.getByUuid(savedTag.getUuid());

    assertThat(retrievedTag).isEqualTo(savedTag);
  }

  @DisplayName("can save and successfully delete")
  @Test
  void saveAndDelete() {
    Tag tag = Tag.builder().namespace("tag-namespace").id("tag-id2").tagType("tag-type").build();

    Tag savedTag = repo.save(tag);
    boolean success = repo.delete(savedTag.getUuid());
    assertThat(success).isTrue();

    Tag nonexistingTag = repo.getByUuid(savedTag.getUuid());
    assertThat(nonexistingTag).isNull();
  }
}
