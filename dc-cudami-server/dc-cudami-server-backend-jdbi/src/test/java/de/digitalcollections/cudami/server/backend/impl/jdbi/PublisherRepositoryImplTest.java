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
import de.digitalcollections.model.list.filtering.FilterCriterion;
import de.digitalcollections.model.list.filtering.Filtering;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedText;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    Publisher publisher = buildPublisher();

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
    Publisher publisher = buildPublisher();

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
    Publisher publisher = repo.save(buildPublisher());

    Publisher publisherToUpdate =
        Publisher.builder()
            .agent(publisher.getAgent())
            .location(publisher.getLocations().get(0))
            .publisherPresentation("Ort 1 : Körperschaft")
            .uuid(publisher.getUuid())
            .lastModified(publisher.getLastModified())
            .created(publisher.getCreated())
            .build();

    Publisher updatedPublisher = repo.update(publisherToUpdate);

    assertThat(updatedPublisher).isEqualTo(publisherToUpdate);
  }

  @DisplayName("can retrieve all Publishers with paging")
  @Test
  void findAllPaged() throws RepositoryException {
    Publisher savedPublisher = repo.save(buildPublisher());

    PageResponse<Publisher> pageResponse =
        repo.find(PageRequest.builder().pageNumber(0).pageSize(99).build());
    assertThat(pageResponse.getContent()).containsExactly(savedPublisher);
  }

  @DisplayName("can retrieve all Publishers with sorting")
  @Test
  void findAllPagedAndSorted() throws RepositoryException {
    HumanSettlement place1 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 1"));
    HumanSettlement place2 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 2"));
    CorporateBody corporateBody = ensureCorporateBody(Map.of(Locale.GERMAN, "Publisher"));

    Publisher savedPublisher1 =
        repo.save(
            buildPublisher(List.of(place2, place1), corporateBody, "Ort 2, Ort 1 : Körperschaft"));
    Publisher savedPublisher2 =
        repo.save(
            buildPublisher(List.of(place1, place2), corporateBody, "Ort 1, Ort 2 : Körperschaft"));

    PageResponse<Publisher> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .sorting(
                    Sorting.builder()
                        .order(
                            Order.builder()
                                .property("publisherPresentation")
                                .direction(Direction.ASC)
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedPublisher2, savedPublisher1);
  }

  @DisplayName("can retrieve publishers with filtering of agent and one location")
  @Test
  void findFilteredAgentLocation() throws RepositoryException {
    HumanSettlement place1 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 1"));
    HumanSettlement place2 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 2"));
    CorporateBody corporateBody1 = ensureCorporateBody(Map.of(Locale.GERMAN, "Publisher 1"));
    CorporateBody corporateBody2 = ensureCorporateBody(Map.of(Locale.GERMAN, "Publisher 2"));

    Publisher savedPublisher1 =
        repo.save(buildPublisher(List.of(place1), corporateBody1, "Ort 1 : Körperschaft 1"));
    Publisher savedPublisher2 =
        repo.save(buildPublisher(List.of(place2), corporateBody2, "Ort 2 : Körperschaft 2"));

    PageResponse<Publisher> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("agent_uuid")
                                .isEquals(corporateBody1.getUuid().toString())
                                .build())
                        .add(
                            FilterCriterion.builder()
                                .withExpression("location_uuid")
                                .contains(place1.getUuid().toString())
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedPublisher1);
  }

  @DisplayName(
      "can retrieve publishers with filtering of agent and multiple locations in the given order")
  @Test
  void findFilteredAgentMultipleLocations() throws RepositoryException {
    HumanSettlement place1 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 1"));
    HumanSettlement place2 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 2"));
    CorporateBody corporateBody1 = ensureCorporateBody(Map.of(Locale.GERMAN, "Publisher 1"));

    Publisher savedPublisher =
        repo.save(
            buildPublisher(
                List.of(place1, place2), corporateBody1, "Ort 1, Ort 2 : Körperschaft 1"));

    Publisher unwantedPublisher =
        repo.save(
            buildPublisher(
                List.of(place2, place1), corporateBody1, "Ort 2, Ort 1: Körperschaft 1"));

    PageResponse<Publisher> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("agent_uuid")
                                .isEquals(corporateBody1.getUuid().toString())
                                .build())
                        .add(
                            FilterCriterion.builder()
                                .withExpression("location_uuids")
                                .isEquals(
                                    List.of(
                                        place1.getUuid().toString(), place2.getUuid().toString()))
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedPublisher);
  }

  @DisplayName("can retrieve publishers with filtering of publisherPresentation")
  @Test
  void findFilteredPublisherPresentation() throws RepositoryException {
    HumanSettlement place1 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 1"));
    HumanSettlement place2 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 2"));
    CorporateBody corporateBody1 = ensureCorporateBody(Map.of(Locale.GERMAN, "Publisher 1"));
    CorporateBody corporateBody2 = ensureCorporateBody(Map.of(Locale.GERMAN, "Publisher 2"));

    Publisher savedPublisher1 =
        repo.save(buildPublisher(List.of(place1), corporateBody1, "Ort 1 : Körperschaft 1"));
    Publisher savedPublisher2 =
        repo.save(buildPublisher(List.of(place2), corporateBody2, "Ort 2 : Körperschaft 2"));

    PageResponse<Publisher> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("publisherPresentation")
                                .isEquals(savedPublisher2.getPublisherPresentation())
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).containsExactly(savedPublisher2);
  }

  @DisplayName("can return an empty filtered set when no matches are found")
  @Test
  void noMatches() throws RepositoryException {
    repo.save(buildPublisher());

    PageResponse<Publisher> pageResponse =
        repo.find(
            PageRequest.builder()
                .pageNumber(0)
                .pageSize(99)
                .filtering(
                    Filtering.builder()
                        .add(
                            FilterCriterion.builder()
                                .withExpression("publisherPresentation")
                                .isEquals("nonexistant")
                                .build())
                        .build())
                .build());
    assertThat(pageResponse.getContent()).isEmpty();
  }

  // -------------------------------------------------------------------------------------------------------

  private Publisher buildPublisher() {
    CorporateBody agent =
        ensureCorporateBody(
            Map.of(Locale.GERMAN, "Körperschaft", Locale.ENGLISH, "Corporate Body"));
    HumanSettlement place1 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 1"));
    HumanSettlement place2 = ensureHumanSettlement(Map.of(Locale.GERMAN, "Ort 2"));
    return Publisher.builder()
        .agent(agent)
        .locations(List.of(place1, place2))
        .publisherPresentation("Ort 1, Ort 2 : Körperschaft")
        .build();
  }

  private Publisher buildPublisher(
      List<HumanSettlement> places, CorporateBody corporateBody, String publisherPresentation) {
    return Publisher.builder()
        .locations(places)
        .agent(corporateBody)
        .publisherPresentation(publisherPresentation)
        .build();
  }

  private HumanSettlement ensureHumanSettlement(Map<Locale, String> placeNames) {
    LocalizedText placeName = new LocalizedText();
    placeName.putAll(placeNames);

    return humanSettlementRepository.save(
        HumanSettlement.builder().label(placeName).name(placeName).build());
  }

  private CorporateBody ensureCorporateBody(Map<Locale, String> names) {
    LocalizedText name = new LocalizedText();
    name.putAll(names);
    return corporateBodyRepository.save(CorporateBody.builder().label(name).name(name).build());
  }
}
