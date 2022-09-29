package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.semantic;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.entity.semantic.Subject;
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
    final Identifier identifier1 = Identifier.builder().namespace("namespace1").id("id1").build();
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
}
