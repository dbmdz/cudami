package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.paging.SearchPageRequest;
import de.digitalcollections.model.paging.SearchPageResponse;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = IdentifiableRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The Identifiable Repository")
class IdentifiableRepositoryImplTest {

  IdentifiableRepositoryImpl repo;

  @Autowired PostgreSQLContainer postgreSQLContainer;
  @Autowired Jdbi jdbi;
  @Autowired CudamiConfig cudamiConfig;

  @BeforeEach
  public void beforeEach() {
    repo = new IdentifiableRepositoryImpl(jdbi, cudamiConfig);
  }

  @Test
  @DisplayName("is testable")
  void containerIsUpAndRunning() {
    assertThat(postgreSQLContainer.isRunning()).isTrue();
  }

  @Test
  @DisplayName("retrieve one digital object")
  void testFindOne() {
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(UUID.randomUUID());
    identifiable.setCreated(LocalDateTime.now());
    identifiable.setType(IdentifiableType.RESOURCE);
    identifiable.setLabel("test");
    identifiable.setLastModified(LocalDateTime.now());

    identifiable = this.repo.save(identifiable);

    Identifiable actual = this.repo.findOne(identifiable.getUuid());
    assertThat(actual).isEqualTo(identifiable);
  }

  @Test
  @DisplayName("saves a DigitalObject and fills uuid and timestamps")
  void testSave() {
    DigitalObject digitalObject = new DigitalObject();
    digitalObject.setLabel("Test");
    assertThat(digitalObject.getCreated()).isNull();
    assertThat(digitalObject.getLastModified()).isNull();
    assertThat(digitalObject.getUuid()).isNull();

    DigitalObject actual = (DigitalObject) repo.save(digitalObject);

    assertThat(actual).isEqualTo(digitalObject);
    assertThat(actual.getCreated()).isNotNull();
    assertThat(actual.getLastModified()).isNotNull();
    assertThat(actual.getUuid()).isNotNull();
  }

  @Test
  @DisplayName("returns properly sized pages on search")
  void testSearchPageSize() {
    // Insert a bunch of DigitalObjects with labels
    IntStream.range(0, 20)
        .forEach(
            i -> {
              repo.save(createDigitalObjectWithLabels("test" + i));
            });

    String query = "test";
    SearchPageRequest searchPageRequest = new SearchPageRequest();
    searchPageRequest.setPageSize(10);
    searchPageRequest.setPageNumber(0);
    searchPageRequest.setQuery(query);

    SearchPageResponse response = repo.find(searchPageRequest);

    List<Identifiable> content = response.getContent();
    assertThat(content).hasSize(10);
  }

  @Test
  @DisplayName("returns expected sql string")
  void testGetCommonSearchSql() {
    String actual = repo.getCommonSearchSql("test");
    String expected =
        "(jsonb_path_exists(test.label, ('$.** ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath) OR jsonb_path_exists(test.description, ('$.** ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath))";
    assertThat(actual).isEqualTo(expected);
  }

  private DigitalObject createDigitalObjectWithLabels(String label) {
    DigitalObject digitalObject = new DigitalObject();
    LocalizedText labelText = new LocalizedText();
    labelText.setText(Locale.GERMAN, label);
    labelText.setText(Locale.ENGLISH, label);
    digitalObject.setLabel(labelText);
    LocalizedStructuredContent description = new LocalizedStructuredContent();
    StructuredContent structuredContent = new StructuredContent();
    Paragraph paragraph = new Paragraph(label);
    structuredContent.addContentBlock(paragraph);
    description.put(Locale.GERMAN, structuredContent);
    description.put(Locale.ENGLISH, structuredContent);
    digitalObject.setDescription(description);
    return digitalObject;
  }
}
