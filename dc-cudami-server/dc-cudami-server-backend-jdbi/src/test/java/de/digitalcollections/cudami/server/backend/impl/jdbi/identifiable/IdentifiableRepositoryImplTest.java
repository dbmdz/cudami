package de.digitalcollections.cudami.server.backend.impl.jdbi.identifiable;

import static de.digitalcollections.cudami.server.backend.impl.asserts.CudamiAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import de.digitalcollections.cudami.model.config.CudamiConfig;
import de.digitalcollections.cudami.server.backend.impl.database.config.SpringConfigBackendDatabase;
import de.digitalcollections.model.identifiable.Identifiable;
import de.digitalcollections.model.identifiable.IdentifiableType;
import de.digitalcollections.model.identifiable.entity.DigitalObject;
import de.digitalcollections.model.list.paging.PageRequest;
import de.digitalcollections.model.list.paging.PageResponse;
import de.digitalcollections.model.list.sorting.Direction;
import de.digitalcollections.model.list.sorting.Order;
import de.digitalcollections.model.list.sorting.Sorting;
import de.digitalcollections.model.text.LocalizedStructuredContent;
import de.digitalcollections.model.text.LocalizedText;
import de.digitalcollections.model.text.StructuredContent;
import de.digitalcollections.model.text.contentblock.Paragraph;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.MOCK, classes = IdentifiableRepositoryImpl.class)
@ContextConfiguration(classes = SpringConfigBackendDatabase.class)
@DisplayName("The Identifiable Repository")
@Sql(scripts = "classpath:cleanup_database.sql")
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

  @Test
  @DisplayName("retrieve one digital object")
  void testGetByUuid() {
    Identifiable identifiable = new Identifiable();
    identifiable.setUuid(UUID.randomUUID());
    identifiable.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
    identifiable.setType(IdentifiableType.RESOURCE);
    identifiable.setLabel("test");
    identifiable.setLastModified(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));

    identifiable = this.repo.save(identifiable);

    Identifiable actual = this.repo.getByUuid(identifiable.getUuid());
    assertThat(actual).isEqualTo(identifiable);
  }

  @Test
  @DisplayName("returns expected sql string")
  void testGetCommonSearchSql() {
    String actual = repo.getCommonSearchSql("test");
    String expected =
        "(jsonb_path_exists(test.label, ('$.** ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath) OR jsonb_path_exists(test.description, ('$.** ? (@ like_regex \"' || :searchTerm || '\" flag \"iq\")')::jsonpath))";
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  @DisplayName("test getOrderBy")
  void testGetOrderBy() {
    Sorting sorting = new Sorting(new Order(Direction.DESC, "lastModified"), new Order("uuid"));
    assertThat(repo.getOrderBy(sorting)).isEqualTo("i.last_modified DESC, i.uuid ASC");

    sorting = new Sorting(Order.builder().property("label").subProperty("de").build());
    assertThat(repo.getOrderBy(sorting))
        .isEqualTo("lower(COALESCE(i.label->>'de', i.label->>'')) COLLATE \"ucs_basic\" ASC");

    sorting.getOrders().add(new Order(Direction.DESC, "lastModified"));
    assertThat(repo.getOrderBy(sorting))
        .isEqualTo(
            "lower(COALESCE(i.label->>'de', i.label->>'')) COLLATE \"ucs_basic\" ASC, i.last_modified DESC");

    sorting =
        new Sorting(
            new Order(Direction.DESC, "created"),
            Order.builder().property("label").subProperty("de").build(),
            Order.builder()
                .property("label")
                .subProperty("en")
                .direction(Direction.DESC)
                .ignoreCase(false)
                .build());
    assertThat(repo.getOrderBy(sorting))
        .isEqualTo(
            "i.created DESC, "
                + "lower(COALESCE(i.label->>'de', i.label->>'')) COLLATE \"ucs_basic\" ASC, "
                + "COALESCE(i.label->>'en', i.label->>'') COLLATE \"ucs_basic\" DESC");
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

    String searchTerm = "test";
    PageRequest pageRequest = new PageRequest();
    pageRequest.setPageSize(10);
    pageRequest.setPageNumber(0);
    pageRequest.setSearchTerm(searchTerm);

    PageResponse response = repo.find(pageRequest);

    List<Identifiable> content = response.getContent();
    assertThat(content).hasSize(10);
  }
}
