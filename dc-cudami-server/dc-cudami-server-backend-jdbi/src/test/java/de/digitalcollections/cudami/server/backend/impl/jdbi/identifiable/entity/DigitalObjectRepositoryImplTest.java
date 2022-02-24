package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.jdbi.legal.LicenseRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.entity.DigitalObjectBuilder;
import de.digitalcollections.model.identifiable.resource.FileResource;
import de.digitalcollections.model.legal.License;
import de.digitalcollections.model.legal.LicenseBuilder;
import de.digitalcollections.model.paging.Direction;
import de.digitalcollections.model.paging.OrderBuilder;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.paging.Sorting;
import de.digitalcollections.model.text.contentblock.Paragraph;
import de.digitalcollections.model.text.contentblock.Text;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;
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
@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = {DigitalObjectRepositoryImpl.class})
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The DigitalObject Repository")
class DigitalObjectRepositoryImplTest {

  DigitalObjectRepositoryImpl repo;

  @Autowired PostgreSQLContainer postgreSQLContainer;

  @Autowired Jdbi jdbi;

  @Autowired private CollectionRepositoryImpl collectionRepositoryImpl;

  @Autowired
  private FileResourceMetadataRepositoryImpl<FileResource> fileResourceMetadataRepositoryImpl;

  private static final License EXISTING_LICENSE =
      new LicenseBuilder()
          .withUuid(UUID.randomUUID())
          .withAcronym("CC0 1.0")
          .withUrl("http://rightsstatements.org/vocab/NoC-NC/1.0/")
          .withLabel(
              Locale.GERMAN, "Kein Urheberrechtsschutz – nur nicht-kommerzielle Nutzung erlaubt")
          .withLabel(Locale.ENGLISH, "No Copyright – Non-Commercial Use Only")
          .build();

  @BeforeEach
  public void beforeEach() {
    repo = new DigitalObjectRepositoryImpl(jdbi);
    repo.setCollectionRepository(collectionRepositoryImpl);
    repo.setFileResourceMetadataRepository(fileResourceMetadataRepositoryImpl);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @Test
  @DisplayName("should save a DigitalObject")
  void saveDigitalObject() {
    // Insert a license with uuid
    LicenseRepositoryImpl licenseRepository = new LicenseRepositoryImpl(jdbi);
    licenseRepository.save(EXISTING_LICENSE);

    DigitalObject digitalObject =
        new DigitalObjectBuilder()
            .withLabel(Locale.GERMAN, "deutschsprachiges Label")
            .withLabel(Locale.ENGLISH, "english label")
            .withDescription(Locale.GERMAN, "Beschreibung")
            .withDescription(Locale.ENGLISH, "description")
            .withLicense(EXISTING_LICENSE)
            .build();

    DigitalObject actual = repo.save(digitalObject);

    assertThat(actual.getLabel().getText(Locale.GERMAN)).isEqualTo("deutschsprachiges Label");
    assertThat(actual.getLabel().getText(Locale.ENGLISH)).isEqualTo("english label");
    Paragraph paragraphDe =
        (Paragraph) actual.getDescription().get(Locale.GERMAN).getContentBlocks().get(0);
    assertThat(((Text) paragraphDe.getContentBlocks().get(0)).getText()).isEqualTo("Beschreibung");

    assertThat(actual.getLicense().getUuid()).isEqualTo(digitalObject.getLicense().getUuid());
  }

  @Test
  @DisplayName("should return properly sized pages on search")
  void testSearchPageSize() {
    // Insert a bunch of DigitalObjects with labels
    IntStream.range(0, 20)
        .forEach(
            i -> {
              repo.save(
                  TestModelFixture.createDigitalObject(
                      Map.of(Locale.GERMAN, "de labeltest" + i, Locale.ENGLISH, "en labeltest" + i),
                      Map.of(Locale.GERMAN, "de desctest" + i, Locale.ENGLISH, "en desctest" + i)));
            });

    String query = "test";
    SearchPageRequest searchPageRequest = new SearchPageRequest();
    searchPageRequest.setPageSize(10);
    searchPageRequest.setPageNumber(0);
    searchPageRequest.setQuery(query);
    searchPageRequest.setSorting(
        Sorting.defaultBuilder()
            .order(new OrderBuilder().property("refId").direction(Direction.ASC).build())
            .build());

    SearchPageResponse response = repo.find(searchPageRequest);
    assertThat(((SearchPageRequest) response.getPageRequest()).getQuery()).isEqualTo(query);

    List<Identifiable> content = response.getContent();
    assertThat(content).hasSize(10);
  }
}
