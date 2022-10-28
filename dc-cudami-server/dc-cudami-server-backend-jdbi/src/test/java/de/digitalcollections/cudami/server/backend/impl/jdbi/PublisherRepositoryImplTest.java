package de.digitalcollections.cudami.server.backend.impl.jdbi;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.api.repository.exceptions.RepositoryException;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.CorporateBodyRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.agent.PersonRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity.geo.location.HumanSettlementRepositoryImpl;
import de.digitalcollections.model.identifiable.entity.agent.CorporateBody;
import de.digitalcollections.model.identifiable.entity.geo.location.HumanSettlement;
import de.digitalcollections.model.identifiable.entity.work.Publisher;
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
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {PublisherRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@Sql(scripts = "classpath:cleanup_database.sql")
@DisplayName("The Publisher Repository")
class PublisherRepositoryImplTest {

  PublisherRepositoryImpl repo;

  @Autowired CudamiConfig cudamiConfig;

  @Autowired private CorporateBodyRepositoryImpl corporateBodyRepository;

  @Autowired private HumanSettlementRepositoryImpl humanSettlementRepository;

  @Autowired private PersonRepositoryImpl personRepository;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired Jdbi jdbi;

  @BeforeEach
  public void beforeEach() {
    repo =
        new PublisherRepositoryImpl(
            jdbi,
            cudamiConfig,
            corporateBodyRepository,
            personRepository,
            humanSettlementRepository);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @DisplayName("can save and retrieve by uuid")
  @Test
  void saveAndRetrieveByUuid() throws RepositoryException {
    CorporateBody agent =
        corporateBodyRepository.save(
            CorporateBody.builder()
                .label(Locale.GERMAN, "Körperschaft")
                .label(Locale.ENGLISH, "Corporate Body")
                .build());
    HumanSettlement place1 =
        humanSettlementRepository.save(
            HumanSettlement.builder().label(Locale.GERMAN, "Ort 1").build());
    HumanSettlement place2 =
        humanSettlementRepository.save(
            HumanSettlement.builder().label(Locale.GERMAN, "Ort 2").build());
    Publisher publisher =
        Publisher.builder()
            .agent(agent)
            .location(place1)
            .location(place2)
            .publisherPresentation("Ort 1, Ort 2 : Körperschaft")
            .build();

    Publisher savedPublisher = repo.save(publisher);

    assertThat(savedPublisher.getUuid()).isNotNull();
    assertThat(savedPublisher.getCreated()).isNotNull();
    assertThat(savedPublisher.getLastModified()).isNotNull();
    assertThat(savedPublisher.getPublisherPresentation())
        .isEqualTo(publisher.getPublisherPresentation());
    assertThat(savedPublisher.getAgent()).isEqualTo(publisher.getAgent());
    assertThat(savedPublisher.getLocations()).isEqualTo(publisher.getLocations());

    Publisher retrievedPublisher = repo.getByUuid(savedPublisher.getUuid());

    assertThat(retrievedPublisher).isEqualTo(savedPublisher);
  }

  @DisplayName("can save and successfully delete")
  @Test
  void saveAndDelete() throws RepositoryException {
    CorporateBody agent =
        corporateBodyRepository.save(
            CorporateBody.builder()
                .label(Locale.GERMAN, "Körperschaft")
                .label(Locale.ENGLISH, "Corporate Body")
                .build());
    HumanSettlement place1 =
        humanSettlementRepository.save(
            HumanSettlement.builder().label(Locale.GERMAN, "Ort 1").build());
    HumanSettlement place2 =
        humanSettlementRepository.save(
            HumanSettlement.builder().label(Locale.GERMAN, "Ort 2").build());
    Publisher publisher =
        Publisher.builder()
            .agent(agent)
            .location(place1)
            .location(place2)
            .publisherPresentation("Ort 1, Ort 2 : Körperschaft")
            .build();

    Publisher savedPublisher = repo.save(publisher);
    int deleted = repo.deleteByUuid(savedPublisher.getUuid());
    assertThat(deleted).isEqualTo(1);

    Publisher nonexistingPublisher = repo.getByUuid(savedPublisher.getUuid());
    assertThat(nonexistingPublisher).isNull();
  }

  @DisplayName("returns zero, when a deletion did not delete anything")
  @Test
  void deleteNothing() throws RepositoryException {
    assertThat(repo.deleteByUuid(UUID.randomUUID())).isEqualTo(0);
  }

  @DisplayName("can save and update")
  @Test
  void saveAndUpdate() throws RepositoryException {
    CorporateBody agent =
        corporateBodyRepository.save(
            CorporateBody.builder()
                .label(Locale.GERMAN, "Körperschaft")
                .label(Locale.ENGLISH, "Corporate Body")
                .build());
    HumanSettlement place1 =
        humanSettlementRepository.save(
            HumanSettlement.builder().label(Locale.GERMAN, "Ort 1").build());
    HumanSettlement place2 =
        humanSettlementRepository.save(
            HumanSettlement.builder().label(Locale.GERMAN, "Ort 2").build());
    Publisher publisher =
        repo.save(
            Publisher.builder()
                .agent(agent)
                .location(place1)
                .location(place2)
                .publisherPresentation("Ort 1, Ort 2 : Körperschaft")
                .build());

    Publisher publisherToUpdate =
        Publisher.builder()
            .agent(agent)
            .location(place1)
            .publisherPresentation("Ort 1 : Körperschaft")
            .uuid(publisher.getUuid())
            .lastModified(publisher.getLastModified())
            .created(publisher.getCreated())
            .build();

    Publisher updatedPublisher = repo.update(publisherToUpdate);

    assertThat(updatedPublisher).isEqualTo(publisherToUpdate);
  }
}
