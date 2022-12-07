package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.api.repository.identifiable.web.WebpageRepository;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendTestDatabase;
import de.digitalcollections.model.identifiable.Identifier;
import de.digitalcollections.model.identifiable.web.Webpage;
import java.util.List;
import java.util.Locale;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = IdentifiableRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendTestDatabase.class)
@DisplayName("The Identifier Repository")
@Sql(scripts = "classpath:cleanup_database.sql")
class IdentifierRepositoryImplTest {

  IdentifierRepositoryImpl repo;

  @Autowired PostgreSQLContainer postgreSQLContainer;
  @Autowired Jdbi jdbi;
  @Autowired CudamiConfig cudamiConfig;
  @Autowired WebpageRepository webpageRepository;

  @BeforeEach
  public void beforeEach() {
    repo = new IdentifierRepositoryImpl(jdbi, cudamiConfig);
    System.out.println(postgreSQLContainer.getJdbcUrl());
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @Test
  @DisplayName("can save and return the saved object")
  void checkSave() throws RepositoryException {
    UUID identifiableUuid = UUID.randomUUID();
    saveIdentifiable(identifiableUuid);
    Identifier identifier = new Identifier(identifiableUuid, "namespace", "id");

    Identifier persisted = repo.save(identifier);
    assertThat(persisted.getUuid()).isNotNull();
    assertThat(persisted.getIdentifiable()).isEqualTo(identifiableUuid);
    assertThat(persisted.getNamespace()).isEqualTo("namespace");
    assertThat(persisted.getId()).isEqualTo("id");
  }

  @Test
  @DisplayName("can return an empty list when no identifiers were found for an identifiable")
  void retrieveNoIdentifiersForIdentifiable() throws RepositoryException {
    assertThat(repo.findByIdentifiable(UUID.randomUUID())).isEmpty();
  }

  @Test
  @DisplayName("can return a list of identifiers for an identifiable, when retrieved by uuid")
  void identifiersForIdentifiable() throws RepositoryException {
    UUID identifiableUuid = UUID.randomUUID();
    saveIdentifiable(identifiableUuid);
    Identifier identifier1 = repo.save(new Identifier(identifiableUuid, "namespace", "1"));
    Identifier identifier2 = repo.save(new Identifier(identifiableUuid, "namespace", "2"));

    List<Identifier> actual = repo.findByIdentifiable(identifiableUuid);
    assertThat(actual).hasSize(2);
    assertThat(actual).containsExactly(identifier1, identifier2);
  }

  @Test
  @DisplayName("can return an identifier by its uuid")
  void getByUuid() throws RepositoryException {
    // Persist an identifier
    UUID identifiableUuid = UUID.randomUUID();
    saveIdentifiable(identifiableUuid);
    Identifier identifier = new Identifier(identifiableUuid, "namespace", "id");

    Identifier persisted = repo.save(identifier);

    UUID identifierUuid = persisted.getUuid();
    assertThat(identifierUuid).isNotNull();

    // Retrieve it by its uuid - it must be the same as what was returned before at persisting
    Identifier actual = repo.getByUuid(identifierUuid);
    assertThat(actual).isEqualTo(persisted);
  }

  private void saveIdentifiable(UUID uuid) throws RepositoryException {
    Webpage webpage =
        Webpage.builder()
            .label(Locale.GERMAN, String.valueOf(uuid.getMostSignificantBits()))
            .uuid(uuid)
            .build();
    webpageRepository.save(webpage);
  }
}
