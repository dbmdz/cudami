package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.entity;

import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable.resource.FileResourceMetadataRepositoryImpl;
import de.digitalcollections.cudami.server.backend.impl.model.TestModelFixture;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.identifiable.resource.FileResource;
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
    DigitalObject digitalObject =
        TestModelFixture.createDigitalObject(
            Map.of(Locale.GERMAN, "deutschsprachiges Label", Locale.ENGLISH, "english label"),
            Map.of(Locale.GERMAN, "Beschreibung", Locale.ENGLISH, "description"));

    DigitalObject actual = repo.save(digitalObject);

    assertThat(actual.getLabel().getText(Locale.GERMAN)).isEqualTo("deutschsprachiges Label");
    assertThat(actual.getLabel().getText(Locale.ENGLISH)).isEqualTo("english label");
    Paragraph paragraphDe =
        (Paragraph) actual.getDescription().get(Locale.GERMAN).getContentBlocks().get(0);
    assertThat(((Text) paragraphDe.getContentBlocks().get(0)).getText()).isEqualTo("Beschreibung");
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
