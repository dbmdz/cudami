package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.identifiable.IdentifierType;
import java.util.UUID;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = IdentifiableRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
// FIXME After `digitalobjects` is merged into main: @Sql(scripts =
// "classpath:cleanup_database.sql")
@DisplayName("The IdentifierType Repository")
class IdentifierTypeRepositoryImplTest {

  IdentifierTypeRepositoryImpl repo;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired Jdbi jdbi;

  @Autowired CudamiConfig cudamiConfig;

  @BeforeEach
  public void beforeEach() {
    repo = new IdentifierTypeRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("can save a new identifier type")
  void saveNewIdentifierType() {
    IdentifierType expected = new IdentifierType();
    expected.setLabel("type-label");
    expected.setNamespace("type-namespace-" + System.currentTimeMillis()); // Namespace is PK
    expected.setPattern("type-pattern");

    IdentifierType actual = repo.save(expected);

    assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
    assertThat(actual.getNamespace()).isEqualTo(expected.getNamespace());
    assertThat(actual.getPattern()).isEqualTo(expected.getPattern());
    assertThat(actual.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("can retrieve an identifier type by uuid")
  void getByUuid() {
    IdentifierType expected = new IdentifierType();
    expected.setLabel("type-label");
    expected.setNamespace("type-namespace-" + System.currentTimeMillis());
    expected.setPattern("type-pattern");

    IdentifierType persisted = repo.save(expected);

    IdentifierType actual = repo.getByUuid(persisted.getUuid());
    assertThat(actual).isNotNull();
    assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
    assertThat(actual.getNamespace()).isEqualTo(expected.getNamespace());
    assertThat(actual.getPattern()).isEqualTo(expected.getPattern());
    assertThat(actual.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("returns null when no identifier type by uuid was found")
  void getByUuidNotFound() {
    IdentifierType expected = new IdentifierType();
    expected.setLabel("type-label");
    expected.setNamespace("type-namespace-" + System.currentTimeMillis());
    expected.setPattern("type-pattern");
    repo.save(expected);

    IdentifierType actual = repo.getByUuid(UUID.randomUUID());
    assertThat(actual).isNull();
  }

  @Test
  @DisplayName("can retrieve an identifier type by namespace")
  void getByNamespace() {
    String namespace = "type-namespace-" + System.currentTimeMillis();

    IdentifierType expected = new IdentifierType();
    expected.setLabel("type-label");
    expected.setNamespace(namespace);
    expected.setPattern("type-pattern");

    IdentifierType persisted = repo.save(expected);

    IdentifierType actual = repo.getByNamespace(namespace);
    assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
    assertThat(actual.getNamespace()).isEqualTo(expected.getNamespace());
    assertThat(actual.getPattern()).isEqualTo(expected.getPattern());
    assertThat(actual.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("returns null with no identifier type by namespace was found")
  void getByNamespaceNotFound() {
    IdentifierType expected = new IdentifierType();
    expected.setLabel("type-label");
    expected.setNamespace("type-namespace-" + System.currentTimeMillis());
    expected.setPattern("type-pattern");
    repo.save(expected);

    IdentifierType actual = repo.getByNamespace("nonexistant");
    assertThat(actual).isNull();
  }

  @Test
  @DisplayName("can update an identifier type")
  void update() {
    IdentifierType initial = new IdentifierType();
    initial.setLabel("type-label");
    initial.setNamespace("type-namespace-" + System.currentTimeMillis());
    initial.setPattern("type-pattern");

    IdentifierType expected = repo.save(initial);
    expected.setLabel("otherlabel");
    expected.setPattern("otherpattern");
    expected.setNamespace("othernamespace-" + System.currentTimeMillis());

    IdentifierType actual = repo.update(expected);
    assertThat(actual.getLabel()).isEqualTo(expected.getLabel());
    assertThat(actual.getNamespace()).isEqualTo(expected.getNamespace());
    assertThat(actual.getPattern()).isEqualTo(expected.getPattern());
  }

  @Test
  @DisplayName("can delete an identifier type")
  void delete() {
    IdentifierType initial = new IdentifierType();
    initial.setLabel("type-label");
    initial.setNamespace("type-namespace-" + System.currentTimeMillis());
    initial.setPattern("type-pattern");

    IdentifierType expected = repo.save(initial);

    repo.delete(expected.getUuid());

    IdentifierType actual = repo.getByUuid(expected.getUuid());
    assertThat(actual).isNull();

    expected.setLabel("otherlabel");
    expected.setPattern("otherpattern");
    expected.setNamespace("othernamespace-" + System.currentTimeMillis());
  }
}
